//
// Created by zenghui on 2020/11/16.
//



#include "PushAudioChannel.h"


PushAudioChannel::PushAudioChannel() {

}

PushAudioChannel::~PushAudioChannel() {

}

void PushAudioChannel::initAudioEncoder(int sample_rate_in_hz, int channels) {
    LOGE("通道数%d", channels)
    this->channels = channels;
    audioEncoder = faacEncOpen(sample_rate_in_hz, channels, &inputSamples, &maxOutputBytes);

    faacEncConfigurationPtr audio_config = faacEncGetCurrentConfiguration(audioEncoder);

    audio_config->mpegVersion = MPEG4;//使用mpeg4 标准
    audio_config->aacObjectType = LOW;//LC标准,兼容性最好的级别
    audio_config->inputFormat = FAAC_INPUT_16BIT; //输入比特率
    audio_config->outputFormat = 0;//原始数据 ，而不是ADTS
    audio_config->useTns = 1;//是否降噪
    audio_config->useLfe = 0;//是否开启环绕音

    int ret = faacEncSetConfiguration(audioEncoder, audio_config);
    if (!ret) {
        LOGE("AudioEncoder 参数设置失败")
        return;
    }
    //初始化输出缓冲区
    buffer = new u_char[maxOutputBytes];
}

int PushAudioChannel::getInputSimples() {
    return inputSamples;
}

void PushAudioChannel::encoderData(int8_t *data) {
    int byteLen = faacEncEncode(audioEncoder, reinterpret_cast<int32_t *>(data), inputSamples,
                                buffer,
                                maxOutputBytes);
    if (byteLen > 0) {
        RTMPPacket *packet = new RTMPPacket();
        int body_size = 2 + byteLen;
        RTMPPacket_Alloc(packet, body_size);
        packet->m_body[0] = 0xAF;//编码格式,采样率,比特率,声道类型
        //1是单声道,默认双声道
        if (channels == 1) {
            packet->m_body[0] = 0xAE;
        }
        packet->m_body[1] = 0x01;
        memcpy(&packet->m_body[2], buffer, byteLen);
        packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
        packet->m_nBodySize = body_size;
        //时间戳
        packet->m_nTimeStamp = -1;
        //相对时间戳
        packet->m_hasAbsTimestamp = 1;
        packet->m_nChannel = 11;
        packet->m_headerType = RTMP_PACKET_SIZE_LARGE;

        if (audioCallBack) {
            audioCallBack(packet);
        }
    }
}

void PushAudioChannel::setAudioCallBack(PushAudioChannel::AudioCallBack audioCallBack) {
    this->audioCallBack = audioCallBack;
}

RTMPPacket *PushAudioChannel::getAudioHeader() {
    u_char *ppBuffer;
    u_long buffer_len;
    faacEncGetDecoderSpecificInfo(audioEncoder, &ppBuffer, &buffer_len);

    RTMPPacket *packet = new RTMPPacket;
    int body_size = 2 + buffer_len;
    RTMPPacket_Alloc(packet, body_size);

    // 给packet 赋值
    packet->m_body[0] = 0xAF;//双声道
    if (channels == 1) {
        packet->m_body[0] = 0xAE;//单声道
    }

    packet->m_body[1] = 0x00;

    memcpy(&packet->m_body[2], ppBuffer, buffer_len);


    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nBodySize = body_size;
    packet->m_nTimeStamp = 0;
    packet->m_hasAbsTimestamp = 1;//帧数据 有时间戳
    packet->m_nChannel = 11;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;

    return packet;
}


