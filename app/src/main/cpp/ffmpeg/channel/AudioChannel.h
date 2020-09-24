//
// Created by zenghui on 2020/9/9.
//

#ifndef JNISTUDY_AUDIOCHANNEL_H
#define JNISTUDY_AUDIOCHANNEL_H


#include "BaseChannel.h"
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

extern "C" {
#include <libswresample/swresample.h>
};

class AudioChannel : public BaseChannel {
public:
    AudioChannel(int streamIndex, AVCodecContext *codecContext, AVRational time_base_);

    virtual ~AudioChannel();

    void start();

    void stop();

    void release();

    void audioDecode();

    void audioPlay();

    int getPCMSize();

    //输出缓冲区
    uint8_t *out_buffers = 0;
    //声道数
    int out_channels = 0;
    //采样位数
    int out_sample_size = 0;
    //采样率
    int out_sample_rate = 0;
    //输出缓冲区大小
    int out_buffers_size = 0;
    //音频每一帧的时间戳
    double audio_time;
private:
    pthread_t thread_audio_decode;
    pthread_t thread_audio_play;
    int isPlaying;
    //引擎obj
    SLObjectItf engineObject;
    //引擎接口
    SLEngineItf engineInterface;
    //混音器
    SLObjectItf outputMixObject;
    //播放器
    SLObjectItf bqPlayerObject;
    //播放器接口
    SLPlayItf bqPlayerPlay;
    //播放器队列
    SLAndroidSimpleBufferQueueItf bqPlayerBufferQueue;

    SwrContext *swr_ctx;

};


#endif //JNISTUDY_AUDIOCHANNEL_H
