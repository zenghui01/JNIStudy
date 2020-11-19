//
// Created by zenghui on 2020/10/22.
//

#include <jni.h>
#include <string>
#include "PushVideoChannel.h"
#include "PushAudioChannel.h"
#include "../safe_queue.h"
#include "librtmp/rtmp.h"
#include "VideoChannel.h"


extern "C" JNIEXPORT jstring JNICALL
Java_com_testndk_jnistudy_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
SafeQueue<RTMPPacket *> packets;

PushVideoChannel *mVideoChannel = 0;
PushAudioChannel *mAudioChannel = 0;

pthread_t pid_start;
uint32_t start_time;
bool isStart = 0;

void releaseAvPacket(RTMPPacket **packet) {
    if (packet) {
        RTMPPacket_Free(*packet);
        DELETE(*packet)
    }
}

void packetCallBack(RTMPPacket *packet) {
    if (packet) {
        if (packet->m_nTimeStamp == -1) {
            packet->m_nTimeStamp = RTMP_GetTime() - start_time;
        }
        packets.push(packet);
    } else {
        DELETE(packet)
    }
}


void *task_start(void *args) {
    LOGE("启动开始直播线程任务")
    char *url = static_cast<char *>(args);
    RTMP *rtmp;
    int ret;
    do {
        //分配rtmp内存
        rtmp = RTMP_Alloc();
        //初始话rtmp对象
        RTMP_Init(rtmp);
        //设置推流地址
        ret = RTMP_SetupURL(rtmp, url);
        if (!ret) {
            LOGE("设置推流地址失败");
            break;
        }

        //开启推流
        RTMP_EnableWrite(rtmp);
        //建立连接
        ret = RTMP_Connect(rtmp, 0);
        if (!ret) {
            LOGE("建立连接失败");
            break;
        }
        //建立流
        ret = RTMP_ConnectStream(rtmp, 0);
        if (!ret) {
            LOGE("建立流连接失败");
            break;
        }
        packets.setWorking(1);
        packetCallBack(mAudioChannel->getAudioHeader());
        //记录流开始推送时间
        start_time = RTMP_GetTime();
        RTMPPacket *rtmpPacket;
        LOGE("启动发包%d", isStart)
        //发送rtmp包
        while (isStart) {
            ret = packets.pop(rtmpPacket);
            if (!isStart) {
                break;
            }
            if (!ret) {
                LOGE("数据取出失败");
                continue;
            }
            rtmpPacket->m_nInfoField2 = rtmp->m_stream_id;
            RTMP_SendPacket(rtmp, rtmpPacket, 1);
        }
        releaseAvPacket(&rtmpPacket);
    } while (0);
    isStart = 0;
    packets.setWorking(0);
    packets.clear();
    if (rtmp) {
        RTMP_Free(rtmp);
        RTMP_Close(rtmp);
    }
    DELETE(url);
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_rtmp_Pusher_initRTMP(JNIEnv *env, jobject thiz) {
    LOGE("准备初始化RTMP")
    LOGE("初始化视频通道")
    mVideoChannel = new PushVideoChannel();
    mVideoChannel->setVideoCallback(packetCallBack);
    LOGE("初始化音频通道")
    mAudioChannel = new PushAudioChannel();
    mAudioChannel->setAudioCallBack(packetCallBack);
    packets.setReleaseCallback(releaseAvPacket);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_rtmp_Pusher_startNativeLive(JNIEnv *env, jobject thiz,
                                                         jstring live_url) {
    LOGE("点击开始直播")
    isStart = 1;
    const char *liveUrl = env->GetStringUTFChars(live_url, 0);
    //为什么要这样写
    char *url = new char[strlen(liveUrl) +
                         1];//前面有\0  new char(xxx)返回的是对象  new char[xxx]返回的是一个长度为xxx的char
    strcpy(url, liveUrl);
    //如果不这样写会导致悬空指针,liveUrl会被释放掉
    pthread_create(&pid_start, 0, task_start, url);
    env->ReleaseStringUTFChars(live_url, liveUrl);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_rtmp_Pusher_initVideoEncoderNative(JNIEnv *env, jobject thiz,
                                                                jint width, jint height,
                                                                jint bitrate, jint fps) {
    LOGE("初始化视频编码器")
    if (mVideoChannel) {
        mVideoChannel->initVideoEncoder(width, height, bitrate, fps);
    }
}


extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_rtmp_Pusher_pushCameraFrame(JNIEnv *env, jobject thiz,
                                                         jbyteArray data_) {
    if (!mVideoChannel || !isStart) {
        return;
    }
    auto data = env->GetByteArrayElements(data_, 0);
    mVideoChannel->encodeData(data);
    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_rtmp_Pusher_stopRTMPNative(JNIEnv *env, jobject thiz) {
    isStart = 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_rtmp_Pusher_releaseRTMPNatice(JNIEnv *env, jobject thiz) {
    DELETE(mVideoChannel)
    DELETE(mAudioChannel)
}

extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_rtmp_Pusher_initAudioEncoderNative(JNIEnv *env, jobject thiz,
                                                                jint sample_rate_in_hz,
                                                                jint channel_config) {
    LOGE("初始化音频编码器")
    if (mAudioChannel) {
        mAudioChannel->initAudioEncoder(sample_rate_in_hz, channel_config);
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_testndk_jnistudy_ui_rtmp_Pusher_getInputSimplesNative(JNIEnv *env, jobject thiz) {
    if (mAudioChannel) {
        return mAudioChannel->getInputSimples();
    } else {
        LOGE("获取样本mAudioChannel为空")
        return -1;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_rtmp_Pusher_pushAudioData(JNIEnv *env, jobject thiz,
                                                       jbyteArray data_) {
    if (!mAudioChannel || !isStart) {
        return;
    }
    auto data = env->GetByteArrayElements(data_, 0);
    mAudioChannel->encoderData(data);
    env->ReleaseByteArrayElements(data_, data, 0);
}