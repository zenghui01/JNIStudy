//
// Created by zenghui on 2020/9/9.
//

#ifndef JNISTUDY_BASECHANNEL_H
#define JNISTUDY_BASECHANNEL_H
extern "C" {
#include <libavcodec/avcodec.h>
};

#include "../safe_queue.h"
#include "../macro.h"
#include <pthread.h>

class BaseChannel {
public:
    BaseChannel(int stream_Index, AVCodecContext *codecContext_) : streamIndex(stream_Index),
                                                                   codecContext(codecContext_) {
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
};


#endif //JNISTUDY_BASECHANNEL_H
