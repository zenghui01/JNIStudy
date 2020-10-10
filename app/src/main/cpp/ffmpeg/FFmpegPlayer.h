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
#include <libavutil/time.h>
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

    void onSeek(int duration);

    void setProgressCallback(JavaFFmpegProgressCallback *progressCallback);

    int getDuration();

private:
    //视频解码通道
    VideoChannel *video_channel;
    //音频解码通道
    AudioChannel *audio_channel;
    //输入源地址
    char *data_source;
    //prepare(加载视频)是耗时操作,放在子线程中,pid_prepare 线程id
    pthread_t pid_prepare;
    //start(开始播放)是耗时操作,放在子线程中,pid_start 线程id
    pthread_t pid_start;
    //加载视频成功回调
    JavaFFmpegCallback *callback = 0;
    /**
     * 加载视频异常回调
     */
    JavaFFmpegErrorCallback *errorCallback = 0;

    /**
    * 加载视频异常回调
    */
    JavaFFmpegProgressCallback *progressCallback = 0;
    /**
     * 是否在播放,用于控制音视频解码
     */
    int isPlaying = 0;
    /**
     * 加载音视频包,上下文
     */
    AVFormatContext *avContext;
    /**
     * 视频解码成frame后,绘制到window
     */
    RenderCallback renderCallback;

    pthread_mutex_t seek_mutex = PTHREAD_MUTEX_INITIALIZER;

};


#endif //JNISTUDY_FFMPEGPLAYER_H
