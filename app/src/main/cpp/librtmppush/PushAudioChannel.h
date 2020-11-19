//
// Created by zenghui on 2020/11/16.
//

#ifndef JNISTUDY_PUSHAUDIOCHANNEL_H
#define JNISTUDY_PUSHAUDIOCHANNEL_H


#include <cstdint>
#include <faac.h>
#include <zconf.h>
#include <cstring>
#include "../macro.h"
#include "librtmp/rtmp.h"

class PushAudioChannel {
    typedef void (*AudioCallBack)(RTMPPacket *packet);

public:
    PushAudioChannel();

    virtual ~PushAudioChannel();

    /**
     * 初始化编码器
     * @param sample_rate_in_hz  编码率
     * @param channel_config  声道数
     */
    void initAudioEncoder(int sample_rate_in_hz, int channels);

    int getInputSimples();

    void encoderData(int8_t *data);

    void setAudioCallBack(AudioCallBack audioCallBack);

    RTMPPacket *getAudioHeader();

private:
    /**
     * 编码器每次接受的最大样本数
     */
    unsigned long inputSamples;
    /**
     * 编码器最大的输出个数
     */
    unsigned long maxOutputBytes;
    /**
     * faac编码器
     */
    faacEncHandle audioEncoder;
    /**
     * 输出缓冲区
     */
    u_char *buffer = 0;
    /**
     * 声道数
     */
    int channels = 0;

    AudioCallBack audioCallBack;
};


#endif //JNISTUDY_PUSHAUDIOCHANNEL_H
