//
// Created by zenghui on 2020/9/9.
//



#include "VideoChannel.h"

VideoChannel::VideoChannel(int streamIndex, AVCodecContext *codecContext, AVRational time_base_)
        : BaseChannel(streamIndex, codecContext, time_base_) {
    frames.setSyncCallback(dropAvFrame);
    packets.setSyncCallback(dropAvPackets);
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
    curDuration = -1;

}

void VideoChannel::release() {

}

void VideoChannel::callbackProgress(int duration) {
//    if (curDuration == duration) {
//        return;
//    }
    curDuration = duration;
    if (progressCallback) {
        progressCallback->onProgress(THREAD_CHILD, duration);
    }
}

void VideoChannel::videoPlay() {
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

        //需要根据fps进行休眠
        //When decoding, this signals how much the picture must be delayed.
        //frame->repeat_pict
        //额外延迟时间
        double extra_delay = frame->repeat_pict / (2 * fps);
        //根据fps获取延迟时间
        double base_delay = 1.0 / fps;//fps秒
        //获取到真正的延迟时间
        double real_delay = base_delay + extra_delay;

        //视频每一帧的时间戳
        double video_time = frame->best_effort_timestamp * av_q2d(time_base);
        //音频有可能为空
        if (!audioChannel) {
            av_usleep(real_delay * 1000 * 1000);
            callbackProgress(video_time);
        } else {
            //音频每一帧的时间戳
            double audio_time = audioChannel->audio_time;
            //视频与音频的时间戳
            double time_diff = video_time - audio_time;
            //如果视频播放的比音频快
            if (time_diff > 0) {
                //如果视频播放的速度比音频的快上很多
                if (time_diff > 1) {
                    //两倍的延迟播放,让音频追上视频进度
                    av_usleep(real_delay * 2 * 1000 * 1000);
                } else {
                    //如果差距不大让真正的延迟时间加上相差的事件进行等待
                    av_usleep((real_delay + time_diff) * 1 * 1000 * 1000);
                }
            } else if (time_diff < 0) {
                //视频播放比音频慢,那么就对视频进行丢帧操作
                if (fabs(time_diff) < 0.5) {
                    frames.sync();
                }
            } else {
                //不存在完美的音视频同步
            }
            callbackProgress(audio_time);
        }
        //渲染回调
        if (renderCallback) {
            renderCallback(dst_data[0], codecContext->width, codecContext->height,
                           dst_linesize[0]);
        }
        releaseAvFrame(&frame);
    }
}


void VideoChannel::videoDecode() {
    AVPacket *packet = 0;
    while (isPlaying) {
        if (isPlaying && frames.size() > 100) {
            //休眠 等待队列中的数据被消费
            av_usleep(10 * 1000);//单位 microseconds
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
        if (ret == AVERROR_EOF) {
            LOGE("暂无更多的视频包了,停止");
            packets.setWorking(0);
        } else if (ret == AVERROR(EAGAIN)) {
            //如果报错是eagain放弃当前帧,尝试取下一帧
            continue;
        } else if (ret != 0) {
            //出现异常跳出循环,直接释放该帧
            releaseAvPacket(&packet);
            break;
        }
        releaseAvPacket(&packet);//packet 不需要了 可以释放
        AVFrame *frame = av_frame_alloc();
        ret = avcodec_receive_frame(codecContext, frame);
        if (ret == AVERROR_EOF) {
            LOGE("暂无更多的视频包了,停止");
            packets.setWorking(0);
        } else if (ret == AVERROR(EAGAIN)) {
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

void VideoChannel::setRenderCallback(RenderCallback renderCallback) {
    this->renderCallback = renderCallback;
}

void VideoChannel::setFPS(double fps) {
    this->fps = fps;
}

void VideoChannel::setAudioChannel(AudioChannel *audioChannel) {
    this->audioChannel = audioChannel;
}





