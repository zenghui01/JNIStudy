//
// Created by zenghui on 2020/9/9.
//

#ifndef JNISTUDY_VIDEOCHANNEL_H
#define JNISTUDY_VIDEOCHANNEL_H


#include "BaseChannel.h"
#include "AudioChannel.h"

extern "C" {
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>
};

typedef void (*RenderCallback)(uint8_t *, int, int, int);

class VideoChannel : public BaseChannel {

public:
    VideoChannel(int streamIndex, AVCodecContext *avCodecContext, AVRational time_base_);

    virtual ~VideoChannel();

    void start();

    void stop();

    void release();

    void videoDecode();

    void videoPlay();

    void setRenderCallback(RenderCallback renderCallback);

    void setFPS(double fps);

    void setAudioChannel(AudioChannel *audioChannel);

    void callbackProgress(int duration);

    void setIsPlaying();

private:
    pthread_t thread_video_decode;

    pthread_t thread_video_play;

    int isPlaying;

    RenderCallback renderCallback;

    AudioChannel *audioChannel = 0;

    int curDuration = -1;

    double fps;
};


#endif //JNISTUDY_VIDEOCHANNEL_H
