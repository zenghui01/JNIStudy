//
// Created by zenghui on 2020/10/28.
//


#include "PushVideoChannel.h"


PushVideoChannel::PushVideoChannel() {
    LOGE("初始化mutex")
    pthread_mutex_init(&mutex, 0);
}

PushVideoChannel::~PushVideoChannel() {
    pthread_mutex_destroy(&mutex);
    if (videoEncoder) {
        x264_encoder_close(videoEncoder);
        videoEncoder = 0;
    }
    if (pic_in) {
        x264_picture_clean(pic_in);
        DELETE(pic_in)
    }
}

void PushVideoChannel::initVideoEncoder(int width, int height, int bitrate, int fps) {
    //宽高改变时，正在编码；导致重复初始化
    LOGE("初始化video encoder")
    pthread_mutex_lock(&mutex);
    mWidth = width;
    mHeight = height;
    mFps = fps;
    mBitrate = bitrate;

    y_len = width * height;
    uv_len = y_len / 4;

    if (videoEncoder) {
        x264_encoder_close(videoEncoder);
        videoEncoder = 0;
    }
    if (pic_in) {
        x264_picture_clean(pic_in);
        DELETE(pic_in)
    }

    //初始化x264编码器
    x264_param_t param;
    //设置编码器属性
    //ultrafast 最快
    //zerolatency 零延迟
    x264_param_default_preset(&param, "ultrafast", "zerolatency");
    //编码规格，base_line 3.2
    //https://wikipedia.tw.wjbk.site/wiki/H.264
    param.i_level_idc = 32;
    //输入数据格式为 YUV420P
    param.i_csp = X264_CSP_I420;
    param.i_width = width;
    param.i_height = height;
    //没有B帧 （如果有B帧会影响编码效率）
    param.i_bframe = 0;//重要

    //码率控制方式。CQP(恒定质量)，CRF(恒定码率)，ABR(平均码率)
//    param.rc.i_rc_method = X264_RC_CRF;
    param.rc.i_rc_method = X264_RC_ABR;
    //码率(比特率，单位Kb/s)
    param.rc.i_bitrate = bitrate / 1000;
    //瞬时最大码率
    param.rc.i_vbv_max_bitrate = bitrate / 1000 * 1.2;
    //设置了i_vbv_max_bitrate就必须设置buffer大小，码率控制区大小，单位Kb/s
    param.rc.i_vbv_buffer_size = bitrate / 1000;

    //码率控制不是通过 timebase 和 timestamp，而是通过 fps
    param.b_vfr_input = 0;
    //帧率分子
    param.i_fps_num = fps;
    //帧率分母
    param.i_fps_den = 1;
    param.i_timebase_den = param.i_fps_num;
    param.i_timebase_num = param.i_fps_den;

    //帧距离(关键帧)  2s一个关键帧
    param.i_keyint_max = fps * 2;// 重要
    //是否复制sps和pps放在每个关键帧的前面 该参数设置是让每个关键帧(I帧)都附带sps/pps。
    param.b_repeat_headers = 1;//重要
    //并行编码线程数
    param.i_threads = 1;
    //profile级别，baseline级别
    x264_param_apply_profile(&param, "baseline");
    //输入图像初始化（待编码的图像）
    pic_in = new x264_picture_t;
    x264_picture_alloc(pic_in, param.i_csp, param.i_width, param.i_height);
    //打开编码器
    videoEncoder = x264_encoder_open(&param);
    if (videoEncoder) {
        LOGE("x264编码器打开成功");
    }
    pthread_mutex_unlock(&mutex);
}

/**
 *
 * @param data
 * nv21  转换成  i420
 * nv21数据样式
 * y1   y2   y3   y4
 * y5   y6   y7   y8
 * y10  y11  y12  y13
 * y14  y15  y16  y17    y层数据
 *
 * v1   u1   v2   u2
 *
 * v3   u3   v4   u4
 *
 * i420数据样式
 * y1   y2   y3   y4
 * y5   y6   y7   y8
 * y10  y11  y12  y13
 * y14  y15  y16  y17    y层数据
 *
 * u1   u2   u3   u4     u层数据
 *
 * v1   v2   v3   v4     v层数据
 */
void PushVideoChannel::encodeData(int8_t *data) {
    pthread_mutex_lock(&mutex);
    //复制y层数据
    memcpy(pic_in->img.plane[0], data, y_len);
    //注意:内存地址是连续的
    for (int i = 0; i < uv_len; i++) {
        //u 分量
        *(pic_in->img.plane[1] + i) = *(data + y_len + i * 2 + 1);
        //v 分量
        *(pic_in->img.plane[2] + i) = *(data + y_len + i * 2);
    }
    //编码流程参考:https://www.jianshu.com/p/0c882eca979c
    x264_nal_t *nal = 0;
    int pi_nal;
    x264_picture_t pic_out;
    /**
     * NALU
     * NALU就是NAL UNIT，nal单元。NAL全称Network Abstract Layer, 即网络抽象层，H.264在网络上传输的结构。
     * 一帧图片经过 H.264 编码器之后，就被编码为一个或多个片（slice），而装载着这些片（slice）的载体，就是 NALU 了 。
     * 我们通过x264编码获得一组或者多组 x264_nal_t。
     * 结合RTMP，我们需要区分的是SPS、PPS、关键帧与普通帧：
     *
     * 即:将一整图像编码为 x264_nal_t (我叫他:网络抽象层编码数据包)
     */
    int ret = x264_encoder_encode(videoEncoder, &nal, &pi_nal, pic_in, &pic_out);
    if (ret < 0) {
        LOGE("编码失败")
        pthread_mutex_unlock(&mutex);
        return;
    }
    int sps_len, pps_len;

    uint8_t sps[100];
    uint8_t pps[100];
    /**
     * 从网络抽象层编码数据包中取出sps以及pps
     * 序列参数集（SPS），包括与图像序列（定义为两个IDR图像间的所有图像）有关的所有信息，应用于已编码视频序列。
　　  * 图像参数集(PPS)，包含所有属于该图像的片的相关信息，用于解码已编码视频序列中的1个或多个独立的图像。
     */
    for (int i = 0; i < pi_nal; i++) {
        if (nal[i].i_type == NAL_SPS) {
            //为什么这里要-4,因为前四位是 x264_nal_t 包的起始码
            sps_len = nal[i].i_payload - 4;
            memcpy(sps, nal[i].p_payload + 4, sps_len);
        } else if (nal[i].i_type == NAL_PPS) {
            //为什么这里要-4,因为前四位是 x264_nal_t 包的起始码
            pps_len = nal[i].i_payload - 4;
            //为什么要+4,前四位位起始码进行跳过
            memcpy(pps, nal[i].p_payload + 4, pps_len);
            /**
             * 将数据封装成RTMPPacket包
             */
            sendSpsPps(sps, pps, sps_len, pps_len);
        } else {
            sendFrame(nal[i].i_type, nal[i].p_payload, nal[i].i_payload);
        }
    }
    pthread_mutex_unlock(&mutex);
}

/**
 * 将数据封装成RTMPPacket
 * @param sps 序列参数集
 * @param pps 图像参数集
 * @param sps_len  序列参数集长度
 * @param pps_len  图像参数集长度
 * RTMPPacket 数据部分
 * body_size
 * 参考 https://www.jianshu.com/p/0c882eca979c
 * 视频包部分(起头部分)
 * sps和pps  0x17    0x00    0x00    0x00    0x00   sps与pps数据
 * sps与pps数据:(数据部分)
 * configurationVersion         1           0x01 版本
 * avProfileIndication          1           sps[1] profile
 * profile_compatibility        1           sps[2] 兼容性
 * profile_level                1           sps[3] profile level
 * lengthSizeMinusOne           1           0xff包长数据所使用的的字节数
 * numOfSequenceParameterSets   1           0xe1 sps个数,通常为0xe1
 * sequenceParameterSetLength   2           sps长度(需要将sps长度转换成2个字节来表示)
 * sequenceParameterSetNALUnits sps长度      sps内容
 * numOfPictureParameterSets    1           0x01 pps个数
 * pictureParameterSetLength    2           pps长度(需要将pps长度转换成2个字节来表示)
 * pictureParameterSetNALUnits  pps长度      pps内容
 *
 */
void PushVideoChannel:: sendSpsPps(uint8_t *sps, uint8_t *pps, int sps_len, int pps_len) {
    //组 RTMPPacket 包
    RTMPPacket *packet = new RTMPPacket;
    int body_size = 5 + 8 + sps_len + 3 + pps_len;
    RTMPPacket_Alloc(packet, body_size);
    // 给packet 赋值
    int i = 0;
    //起头部分
    packet->m_body[i++] = 0x17;

    packet->m_body[i++] = 0x00;
    packet->m_body[i++] = 0x00;
    packet->m_body[i++] = 0x00;
    packet->m_body[i++] = 0x00;
    //数据部分
    //configurationVersion
    packet->m_body[i++] = 0x01;
    //avProfileIndication
    packet->m_body[i++] = sps[1];
    //profile_compatibility
    packet->m_body[i++] = sps[2];
    //profile_level
    packet->m_body[i++] = sps[3];
    //lengthSizeMinusOne
    packet->m_body[i++] = 0xFF;
    //numOfSequenceParameterSets
    packet->m_body[i++] = 0xE1;
    //sequenceParameterSetLength(2个字节表示sps长度)
    packet->m_body[i++] = (sps_len >> 8) & 0xFF;
    packet->m_body[i++] = sps_len & 0xFF;
    //sequenceParameterSetNALUnits sps内容
    memcpy(&packet->m_body[i], sps, sps_len);

    i += sps_len;//sps 拷贝后 i要相应 移位

    //numOfPictureParameterSets
    packet->m_body[i++] = 0x01;
    //pictureParameterSetLength(2个字节表示pps长度)
    packet->m_body[i++] = (pps_len >> 8) & 0xFF;
    packet->m_body[i++] = pps_len & 0xFF;
    //pictureParameterSetNALUnits pps内容
    memcpy(&packet->m_body[i], pps, pps_len);

    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = body_size;
    //时间戳
    packet->m_nTimeStamp = 0;
    //相对时间戳
    packet->m_hasAbsTimestamp = 0;
    packet->m_nChannel = 10;
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;

    if (callback) {
        callback(packet);
    } else {
        LOGE("???")
    }
}


/**
 *
 * @param type  是否是关键帧
 * @param payload
 * @param payload_length
 */
void PushVideoChannel::sendFrame(int type, uint8_t *payload, int iPayload) {
    //一段包含了N个图像的H.264裸数据，每个NAL之间由：00 00 00 01 或者 00 00 01进行分割。
    if (payload[2] == 0x00) {
        //位移4位,去除分割数据
        payload += 4;
        //位移了4位那么长度就要减4
        iPayload -= 4;
    } else if (payload[2] == 0x01) {
        //位移4位
        payload += 3;
        //位移了4位那么长度就要减4
        iPayload -= 3;
    }
    RTMPPacket *packet = new RTMPPacket;
    int body_size = 5 + 4 + iPayload;
    RTMPPacket_Alloc(packet, body_size);
    //起头部分
    packet->m_body[0] = 0x27;
    //如果是关键帧
    if (type == NAL_SLICE_IDR) {
        packet->m_body[0] = 0x17;
    }
    packet->m_body[1] = 0x01;
    packet->m_body[2] = 0x00;
    packet->m_body[3] = 0x00;
    packet->m_body[4] = 0x00;
    //(4个字节长度,表示裸数据长度)
    packet->m_body[5] = (iPayload >> 24) & 0xFF;
    packet->m_body[6] = (iPayload >> 16) & 0xFF;
    packet->m_body[7] = (iPayload >> 8) & 0xFF;
    packet->m_body[8] = iPayload & 0xFF;

    memcpy(&packet->m_body[9], payload, iPayload);


    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = body_size;
    //时间戳
    packet->m_nTimeStamp = -1;
    //相对时间戳
    packet->m_hasAbsTimestamp = 1;
    packet->m_nChannel = 10;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;

    if (callback) {
        callback(packet);
    }
}


void PushVideoChannel::setVideoCallback(PushVideoChannel::VideoCallback callback) {
    this->callback = callback;
}


