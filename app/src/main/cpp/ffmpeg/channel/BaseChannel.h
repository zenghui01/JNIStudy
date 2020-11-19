//
// Created by zenghui on 2020/9/9.
//

#ifndef JNISTUDY_BASECHANNEL_H
#define JNISTUDY_BASECHANNEL_H
extern "C" {
#include <libavcodec/avcodec.h>
#include <libavutil/time.h>
};

#include "../../safe_queue.h"
#include "../../macro.h"
#include <pthread.h>
#include "../java_callback/JavaProgressCallback.h"

class BaseChannel {
public:
    /**
     *
     * @param stream_Index 流中包的index用于判断包是音频包还是视频包
     * @param codecContext_  流中解码上下文
     * @param time_base_  流中时间基数
     */
    BaseChannel(int stream_Index, AVCodecContext *codecContext_, AVRational time_base_,
                jlong file_duration)
            : streamIndex(stream_Index),
              codecContext(codecContext_), time_base(time_base_), file_duration(file_duration) {
        packets.setReleaseCallback(releaseAvPacket);
        frames.setReleaseCallback(releaseAvFrame);
    }

    virtual ~BaseChannel() {
        packets.clear();
        frames.clear();
    }

    //只要出现音频快于视频,就会调用该方法
    static void dropAvFrame(queue<AVFrame *> &frames) {
        if (!frames.empty()) {
            AVFrame *frame = frames.front();
            BaseChannel::releaseAvFrame(&frame);
            frames.pop();
        }
    }

    //只要出现音频快于视频,就会调用该方法
    static void dropAvPackets(queue<AVPacket *> &packets) {
        //当不为空是,遇到关键帧跳出循环,非关键帧直接丢弃
        while (!packets.empty()) {
            AVPacket *packet = packets.front();
            if (packet->flags == AV_PKT_FLAG_KEY) {
                break;
            }
            BaseChannel::releaseAvPacket(&packet);
            packets.pop();
        }
    }

    static void releaseAvPacket(AVPacket **packet) {
        if (packet) {
            av_packet_free(packet);
            DELETE(*packet)
        }
    }

    static void releaseAvFrame(AVFrame **frame) {
        if (frame) {
            av_frame_free(frame);
            DELETE(*frame)
        }
    }


    void setProgressCallback(JavaProgressCallback *progressCallback) {
        this->progressCallback = progressCallback;
    }

    int streamIndex;
    SafeQueue<AVFrame *> frames;//帧包
    SafeQueue<AVPacket *> packets; //码流包
    AVCodecContext *codecContext;
    AVRational time_base;
    JavaProgressCallback *progressCallback;
    int isPlaying;
    jlong file_duration;
};


#endif //JNISTUDY_BASECHANNEL_H
