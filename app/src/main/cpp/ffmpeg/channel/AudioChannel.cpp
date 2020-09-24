//
// Created by zenghui on 2020/9/9.
//


#include "AudioChannel.h"

AudioChannel::AudioChannel(int streamIndex, AVCodecContext *codecContext, AVRational time_base_)
        : BaseChannel(streamIndex,
                      codecContext, time_base_) {
    //初始化缓存区 out_buffers,如何设置缓冲区大小呢?
    //目标初始话参数 采样率 44100 采样格式16bits(位)=2个字节(1个字节八位) 声道数 2
    //AV_CH_LAYOUT_STEREO 声道类型
    out_channels = av_get_channel_layout_nb_channels(AV_CH_LAYOUT_STEREO);//声道数
    out_sample_size = av_get_bytes_per_sample(AV_SAMPLE_FMT_S16);
    out_sample_rate = 44100;
    out_buffers_size = out_sample_rate * out_channels * out_sample_size;
    out_buffers = static_cast<uint8_t *>(malloc(out_buffers_size));
    memset(out_buffers, 0, out_buffers_size);

    //重采样context初始化
    /**
     * 1.context对象
     * 2.输出声道数
     * 3.输出采样格式
     * 4.输出采样率
     * 5.解码对象的声道数
     * 6.解码对象的采样格式
     * 7.解码对象的采样率
     */
    swr_ctx = swr_alloc_set_opts(0, AV_CH_LAYOUT_STEREO, AV_SAMPLE_FMT_S16, out_sample_rate,
                                 codecContext->channel_layout, codecContext->sample_fmt,
                                 codecContext->sample_rate, 0, 0);
    swr_init(swr_ctx);
}

AudioChannel::~AudioChannel() {

}

void *task_audio_decode(void *args) {
    auto *self = static_cast<AudioChannel *>(args);
    self->audioDecode();
    return 0;
}

void *task_audio_play(void *args) {
    auto *self = static_cast<AudioChannel *>(args);
    self->audioPlay();
    return 0;
}

void AudioChannel::start() {
    isPlaying = 1;
    packets.setWorking(1);
    frames.setWorking(1);
    //视频解码线程
    pthread_create(&thread_audio_decode, 0, task_audio_decode, this);
    //视频播放线程
    pthread_create(&thread_audio_play, 0, task_audio_play, this);
}

void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *args) {
    AudioChannel *audio_channel = static_cast<AudioChannel *>(args);
    int pcm_size = audio_channel->getPCMSize();
    if (pcm_size > 0) {
        (*bq)->Enqueue(bq, audio_channel->out_buffers, pcm_size);
    }
}

int AudioChannel::getPCMSize() {
    //获取pcm
    int pcm_data_size = 0;
    //获取pcm
    //PCM在哪里？frames队列中 : frame->data
    AVFrame *frame = 0;
    while (isPlaying) {
        //取出一针
        int ret = frames.pop(frame);
        if (!isPlaying) {
            //如果停止播放了，跳出循环, 释放 frame
            break;
        }
        if (!ret) {
            continue;
        }
        //pcm的处理逻辑
        //音频播放器的数据格式是我们自己在下面定义的
        //而原始数据（待播放的音频pcm数据）
        //重采样！
        int dst_nb_samples = av_rescale_rnd(swr_get_delay(swr_ctx, frame->sample_rate) +
                                            frame->nb_samples, out_sample_rate, frame->sample_rate,
                                            AV_ROUND_UP);
        //number of samples output per channel
        int samples_per_channel = swr_convert(swr_ctx, &out_buffers, dst_nb_samples,
                                              (const uint8_t **) frame->data,
                                              frame->nb_samples);
//        pcm_data_size = samples_per_channel * out_sample_size ;//每个声道的数据个数
        pcm_data_size = samples_per_channel * out_sample_size * out_channels;//所有声道完整数据
        //25fps 1秒25帧   每帧1/25秒
        //用于获取fps  av_q2d
        //time_base 就是从流中取到的AVRational,在AVRational中有帧数分子与时间基数分子
        // frame->best_effort_timestamp * av_q2d(time_base) 获取到每帧音频的时间戳
        audio_time = frame->best_effort_timestamp * av_q2d(time_base);
        break;
    }//end while
    releaseAvFrame(&frame);
    return pcm_data_size;
}


void AudioChannel::audioDecode() {
    AVPacket *packet = 0;
    while (isPlaying == 1) {
        if (frames.size() > 100) {
            av_usleep(10 * 1000);//microseconds
            continue;
        }
        //从队列中取出带解码包
        int ret = packets.pop(packet);
        //如果停止播放,跳出循环并且释放packet
        if (!isPlaying) {
            break;
        }
        if (!ret) {
            continue;
        }
        //取到带解码的视频包
        ret = avcodec_send_packet(codecContext, packet);
        //0表示成功
        if (ret) {
            break;
        }
        releaseAvPacket(&packet);//packet 不需要了 可以释放
        AVFrame *frame = av_frame_alloc();
        ret = avcodec_receive_frame(codecContext, frame);
        if (ret == AVERROR(EAGAIN)) {
            //如果报错是eagain放弃当前帧,尝试取下一帧
            continue;
        } else if (ret != 0) {
            //出现异常跳出循环,直接释放该帧
            releaseAvFrame(&frame);
            break;
        }
        //取到视频帧后加入到视频帧队列,
        frames.push(frame);
    }
    releaseAvPacket(&packet);
}

void AudioChannel::audioPlay() {
    //1.创建引擎并获取引擎接口
    //创建引擎对象SLObjectItf engineObject
    SLresult result = slCreateEngine(&engineObject, 0, NULL,
                                     0, NULL, NULL);

    if (SL_RESULT_SUCCESS != result) {
        return;
    }

    //初始化引擎
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    //获取引擎接口
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineInterface);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }

    //2.创建混音器
    //通过引擎接口获取混音对象
    result = (*engineInterface)->CreateOutputMix(engineInterface, &outputMixObject, 0, 0, 0);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    //初始化混音对象
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    //不启用混响可以不用获取混音器接口
    // 获得混音器接口
    //result = (*outputMixObject)->GetInterface(outputMixObject, SL_IID_ENVIRONMENTALREVERB,
    //                                         &outputMixEnvironmentalReverb);
    //if (SL_RESULT_SUCCESS == result) {
    //设置混响 ： 默认。
    //SL_I3DL2_ENVIRONMENT_PRESET_ROOM: 室内
    //SL_I3DL2_ENVIRONMENT_PRESET_AUDITORIUM : 礼堂 等
    //const SLEnvironmentalReverbSettings settings = SL_I3DL2_ENVIRONMENT_PRESET_DEFAULT;
    //(*outputMixEnvironmentalReverb)->SetEnvironmentalReverbProperties(
    //       outputMixEnvironmentalReverb, &settings);
    //}
/**
     * 3、创建播放器
     */
    //3.1 配置输入声音信息
    //创建buffer缓冲类型的队列 2个队列
    SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
                                                       2};
    //pcm数据格式
    //SL_DATAFORMAT_PCM：数据格式为pcm格式
    //2：双声道
    //SL_SAMPLINGRATE_44_1：采样率为44100，应用最广的兼容性最好的（b/s）
    //SL_PCMSAMPLEFORMAT_FIXED_16：采样格式为16bit (2字节)
    //SL_PCMSAMPLEFORMAT_FIXED_16：数据大小为16bit
    //SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT：左右声道（双声道）
    //SL_BYTEORDER_LITTLEENDIAN：小端模式
    SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 2, SL_SAMPLINGRATE_44_1,
                                   SL_PCMSAMPLEFORMAT_FIXED_16,
                                   SL_PCMSAMPLEFORMAT_FIXED_16,
                                   SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
                                   SL_BYTEORDER_LITTLEENDIAN};

    //数据源 将上述配置信息放到这个数据源中
    SLDataSource audioSrc = {&loc_bufq, &format_pcm};

    //3.2 配置音轨（输出）
    //设置混音器
    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&loc_outmix, NULL};
    //需要的接口 操作队列的接口
    const SLInterfaceID ids[1] = {SL_IID_BUFFERQUEUE};
    const SLboolean req[1] = {SL_BOOLEAN_TRUE};
    //3.3 创建播放器 SLObjectItf bqPlayerObject
    result = (*engineInterface)->CreateAudioPlayer(engineInterface, &bqPlayerObject, &audioSrc,
                                                   &audioSnk, 1, ids, req);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    //3.4 初始化播放器：SLObjectItf bqPlayerObject
    result = (*bqPlayerObject)->Realize(bqPlayerObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    //3.5 获取播放器接口：SLPlayItf bqPlayerPlay
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_PLAY, &bqPlayerPlay);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    /**
     * 4、设置播放回调函数
     */
    //4.1 获取播放器队列接口：SLAndroidSimpleBufferQueueItf bqPlayerBufferQueue
    (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_BUFFERQUEUE, &bqPlayerBufferQueue);

    //4.2 设置回调 void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context)
    (*bqPlayerBufferQueue)->RegisterCallback(bqPlayerBufferQueue, bqPlayerCallback, this);
    /**
     * 5、设置播放器状态为播放状态
     */
    (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PLAYING);
    /**
     * 6、手动激活回调函数
     */
    bqPlayerCallback(bqPlayerBufferQueue, this);
}

void AudioChannel::stop() {

}

void AudioChannel::release() {

}





