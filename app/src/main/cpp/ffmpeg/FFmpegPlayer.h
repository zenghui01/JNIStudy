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
#include "macro.h"
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

private:
    char *data_source;
    char *path = "sdcard/demo.mp4";

    pthread_t pid_prepare{};

    AVFormatContext *formatContext;

    VideoChannel *video_channel;

    AudioChannel *audio_channel;

    JavaFFmpegCallback *callback = 0;
    JavaFFmpegErrorCallback *errorCallback = 0;
};


#endif //JNISTUDY_FFMPEGPLAYER_H
