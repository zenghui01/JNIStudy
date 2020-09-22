//
// Created by zenghui on 2020/9/9.
//



#include "VideoChannel.h"


VideoChannel::VideoChannel(int streamIndex, AVCodecContext *codecContext) : BaseChannel(streamIndex,
                                                                                        codecContext) {

}

VideoChannel::~VideoChannel() {

}

void *task_video_decode(void *args) {
    auto *self = static_cast<VideoChannel *>(args);
    self->videoDecode();
    return 0;
}

void *task_video_play(void *args) {
    auto *self = static_cast<VideoChannel *>(args);
    self->videoPlay();
    return 0;
}


void VideoChannel::start() {
    isPlaying = 1;
    packets.setWorking(1);
    frames.setWorking(1);
    //视频解码线程
    pthread_create(&thread_video_decode, 0, task_video_decode, this);
    //视频播放线程
    pthread_create(&thread_video_play, 0, task_video_play, this);
}

void VideoChannel::stop() {

}

void VideoChannel::release() {

}

void VideoChannel::videoPlay() {
    LOGE("启动播放操作 %d", isPlaying);
    uint8_t *dst_data[4];//长度为什么是4?因为三原色和1个透明度
    int dst_linesize[4];
    AVFrame *frame = 0;
    SwsContext *swsContext = sws_getContext(codecContext->width, codecContext->height,
                                            codecContext->pix_fmt,
                                            codecContext->width, codecContext->height,
                                            AV_PIX_FMT_RGBA,
                                            SWS_BILINEAR, NULL, NULL, NULL);
    //给dst_data分配内存
    av_image_alloc(dst_data, dst_linesize, codecContext->width, codecContext->height,
                   AV_PIX_FMT_RGBA, 1);

    while (isPlaying) {
        int ret = frames.pop(frame);
        if (!isPlaying) {
            break;
        }
        if (!ret) {
            continue;
        }
        //将yuv原始数据转换成rgba
        sws_scale(swsContext, frame->data, frame->linesize, 0, frame->height, dst_data,
                  dst_linesize);
        if (renderCallback) {
            renderCallback(dst_data[0], codecContext->width, codecContext->height, dst_linesize[0]);
        }
        releaseAvFrame(&frame);
    }

}

void VideoChannel::videoDecode() {
    AVPacket *packet = 0;
    while (isPlaying == 1) {
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
            LOGE("获取解码包失败");
            break;
        }
        releaseAvPacket(&packet);//packet 不需要了 可以释放
        AVFrame *frame = av_frame_alloc();
        ret = avcodec_receive_frame(codecContext, frame);
        if (ret == AVERROR(EAGAIN)) {
            //如果报错是eagain放弃当前帧,尝试取下一帧
            continue;
        } else if (ret != 0) {
            LOGE("跳出4%s", av_err2str(AVERROR(ret)));
            break;
        }
        //取到视频帧后加入到视频帧队列,
        frames.push(frame);
    }
    LOGE("释放解码包");
    releaseAvPacket(&packet);
}

void VideoChannel::setRenderCallback(RenderCallback renderCallback) {
    this->renderCallback = renderCallback;
}



