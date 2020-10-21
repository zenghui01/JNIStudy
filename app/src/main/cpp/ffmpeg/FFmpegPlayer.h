//
// Created by zenghui on 2020/9/9.
//

#ifndef JNISTUDY_FFMPEGPLAYER_H
#define JNISTUDY_FFMPEGPLAYER_H

#include <cstring>
#include <pthread.h>
#include "channel/VideoChannel.h"
#include "channel/AudioChannel.h"
#include "java_callback/JavaCallback.h"
#include "../macro.h"
#include "java_callback/JavaErrorCallback.h"
#include <string.h>

extern "C" {
#include <libavformat/avformat.h>
#include <libavutil/time.h>
};


class FFmpegPlayer {
    friend void *task_stop(void *args);

public:
    FFmpegPlayer(const char *string);

    FFmpegPlayer(VideoChannel *videoChannel);

    ~FFmpegPlayer();

    void prepare();

    void pause();

    void player_prepare();

    void setFFmpegCallback(JavaCallback *callback);

    void setFFmpegErrorCallback(JavaErrorCallback *errorCallback);

    void onError(int code, const char *errorMsg);

    void start();

    void stop();

    void release();

    void player_start();

    void setRenderCallback(RenderCallback renderCallback);

    void setCompleteCallback(JavaCompleteCallback *completeCallback);

    void onSeek(int duration);

    void setProgressCallback(JavaProgressCallback *progressCallback);

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
    //stop停止播放
    pthread_t pid_stop;
    //加载视频成功回调
    JavaCallback *loadSuccessCallback = 0;
    /**
     * 加载视频异常回调
     */
    JavaErrorCallback *errorCallback = 0;

    /**
    * 加载视频异常回调
    */
    JavaProgressCallback *progressCallback = 0;
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
    /**
     * seek拖动锁
     */
    pthread_mutex_t seek_mutex = PTHREAD_MUTEX_INITIALIZER;

    /**
     * 视频结束回调
     */
    JavaCompleteCallback *completeCallback;
    /**
     * 标记视频流已经读取完,并不表示播放完,开始播放时和seek时重置状态
     * 0未读取完,1读取完了
     */
    int hasReadEnd = 0;
    /**
     * 表示已经播放结束
     */
    int hasPlayComplete = 0;
};


#endif //JNISTUDY_FFMPEGPLAYER_H
