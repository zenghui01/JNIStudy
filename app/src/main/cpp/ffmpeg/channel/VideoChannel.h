//
// Created by zenghui on 2020/9/9.
//

#ifndef JNISTUDY_VIDEOCHANNEL_H
#define JNISTUDY_VIDEOCHANNEL_H


#include "BaseChannel.h"
#include "AudioChannel.h"
#include "../java_callback/JavaCompleteCallback.h"

extern "C" {
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>
};

typedef void (*RenderCallback)(uint8_t *, int, int, int);

class VideoChannel : public BaseChannel {
public:
    typedef struct {
        double frame_delay;
        double fps;
        RenderCallback renderCallback = 0;
    } VideoParam;

    VideoChannel(int streamIndex, AVCodecContext *avCodecContext, AVRational time_base_,
                 jlong file_duration);

    virtual ~VideoChannel();

    void start();

    void stop();

    void release();

    void videoDecode();

    void videoPlay();

    void setVideoParam(VideoParam *param);

    void callbackProgress(int duration);

    void setAudioChannel(AudioChannel *audioChannel);

private:
    pthread_t thread_video_decode;

    pthread_t thread_video_play;

    int curDuration = -1;

    VideoParam *param;

    AudioChannel *audioChannel = 0;
};


#endif //JNISTUDY_VIDEOCHANNEL_H
