//
// Created by zenghui on 2020/9/9.
//

#ifndef JNISTUDY_VIDEOCHANNEL_H
#define JNISTUDY_VIDEOCHANNEL_H


#include "BaseChannel.h"

extern "C" {
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>
};

typedef void (*RenderCallback)(uint8_t *, int, int, int);

class VideoChannel : public BaseChannel {

public:
    VideoChannel(int streamIndex, AVCodecContext *pContext);

    virtual ~VideoChannel();

    void start();

    void stop();

    void release();

    void videoDecode();

    void videoPlay();

    void setRenderCallback(RenderCallback renderCallback);

private:
    pthread_t thread_video_decode;
    pthread_t thread_video_play;
    int isPlaying;

    RenderCallback renderCallback;
};


#endif //JNISTUDY_VIDEOCHANNEL_H
