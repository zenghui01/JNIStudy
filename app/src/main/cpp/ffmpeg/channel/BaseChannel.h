//
// Created by zenghui on 2020/9/9.
//

#ifndef JNISTUDY_BASECHANNEL_H
#define JNISTUDY_BASECHANNEL_H
extern "C" {
#include <libavcodec/avcodec.h>
#include <libavutil/time.h>
};

#include "../safe_queue.h"
#include "../macro.h"
#include <pthread.h>

class BaseChannel {
public:
    /**
     *
     * @param stream_Index 流中包的index用于判断包是音频包还是视频包
     * @param codecContext_  流中解码上下文
     * @param time_base_  流中时间基数
     */
    BaseChannel(int stream_Index, AVCodecContext *codecContext_, AVRational time_base_)
            : streamIndex(stream_Index),
              codecContext(codecContext_), time_base(time_base_) {
        packets.setReleaseCallback(releaseAvPacket);
        frames.setReleaseCallback(releaseAvFrame);
    }

    virtual ~BaseChannel() {
        packets.clear();
        frames.clear();
    }

    static void releaseAvPacket(AVPacket **packet) {
        if (packet) {
            av_packet_free(packet);
            *packet = 0;
        }
    }

    static void releaseAvFrame(AVFrame **frame) {
        if (frame) {
            av_frame_free(frame);
            *frame = 0;
        }
    }

    int streamIndex;
    SafeQueue<AVFrame *> frames;//帧包
    SafeQueue<AVPacket *> packets; //码流包
    AVCodecContext *codecContext;
    AVRational time_base;
};


#endif //JNISTUDY_BASECHANNEL_H
