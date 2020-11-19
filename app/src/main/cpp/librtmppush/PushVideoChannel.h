//
// Created by zenghui on 2020/10/28.
//

#ifndef JNISTUDY_PUSHVIDEOCHANNEL_H
#define JNISTUDY_PUSHVIDEOCHANNEL_H

#include <pthread.h>
#include <x264.h>
#include "../macro.h"
#include "librtmp/rtmp.h"
#include <__mutex_base>

class PushVideoChannel {
    typedef void (*VideoCallback)(RTMPPacket *rtmpPacket);

public:
    virtual ~PushVideoChannel();

    PushVideoChannel();


    void encodeData(int8_t *data);

    void initVideoEncoder(int width, int height, int bitrate, int fps);

    void sendSpsPps(uint8_t sps[100], uint8_t pps[100], int len, int len1);

    void sendFrame(int type, uint8_t *payload, int payload_length);

    void setVideoCallback(VideoCallback callback);

    pthread_mutex_t mutex;
    int mWidth;
    int mHeight;
    int mFps;
    int mBitrate;
    int y_len;
    int uv_len;
    x264_t *videoEncoder = 0;
    x264_picture_t *pic_in = 0;
    VideoCallback callback;

};


#endif //JNISTUDY_PUSHVIDEOCHANNEL_H
