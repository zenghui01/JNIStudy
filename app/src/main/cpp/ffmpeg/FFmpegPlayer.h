//
// Created by zenghui on 2020/9/9.
//

#ifndef JNISTUDY_FFMPEGPLAYER_H
#define JNISTUDY_FFMPEGPLAYER_H

#include <cstring>
#include <pthread.h>
#include "channel/VideoChannel.h"
#include "channel/AudioChannel.h"
#include "java_callback/JavaFFmpegCallback.h"
#include "../macro.h"
#include "java_callback/JavaFFmpegErrorCallback.h"
#include <string.h>

extern "C" {
#include <libavformat/avformat.h>
};


class FFmpegPlayer {
public:
    FFmpegPlayer(const char *string);

    ~FFmpegPlayer();

    void prepare();

    void player_prepare();

    void setFFmpegCallback(JavaFFmpegCallback *callback);

    void setFFmpegErrorCallback(JavaFFmpegErrorCallback *errorCallback);

    void onError(int code, const char *errorMsg);

    void start();

    void stop();

    void release();

    void player_start();

    void setRenderCallback(RenderCallback renderCallback);

private:
    VideoChannel *video_channel;

    AudioChannel *audio_channel;

    char *data_source;

    pthread_t pid_prepare;

    pthread_t pid_start;

    JavaFFmpegCallback *callback = 0;

    JavaFFmpegErrorCallback *errorCallback = 0;

    int isPlaying = 0;

    AVFormatContext *avContext;

    RenderCallback  renderCallback;
};


#endif //JNISTUDY_FFMPEGPLAYER_H
