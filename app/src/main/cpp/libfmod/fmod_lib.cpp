//
// Created by zenghui on 2020/3/17.
//

#include <jni.h>
#include <string>
#include <unistd.h>
#include "fmod.hpp"
#include "../macro.h"


#undef com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_NORMAL
#define com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_NORMAL 0L
#undef com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_LUOLI
#define com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_LUOLI 1L
#undef com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_DASHU
#define com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_DASHU 2L
#undef com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_JINGSONG
#define com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_JINGSONG 3L
#undef com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_GAOGUAI
#define com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_GAOGUAI 4L
#undef com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_KONGLING
#define com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_KONGLING 5L

using namespace FMOD;

bool isResume = true;

extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_activity_FmodActivity_playVoice(JNIEnv *env, jobject thiz, jint model,
                                                             jstring file_path_) {
    jclass clazz = env->GetObjectClass(thiz);
    jmethodID methodId = env->GetMethodID(clazz, "onFomdCallback", "(Ljava/lang/String;)V");
    isResume = true;
    const char *file_path = env->GetStringUTFChars(file_path_, 0);
    //string需要注意
    env->CallVoidMethod(thiz, methodId, env->NewStringUTF(file_path));
    System *system;
    Sound *sound;
    Channel *channel = 0;
    DSP *dsp = 0;
    //创建系统
    System_Create(&system);
    env->CallVoidMethod(thiz, methodId, env->NewStringUTF("System_Create"));
    //初始化系统
    system->init(32, FMOD_INIT_NORMAL, 0);
    env->CallVoidMethod(thiz, methodId, env->NewStringUTF("System init"));
    //创建声音
    system->createSound(file_path, FMOD_DEFAULT, 0, &sound);
    env->CallVoidMethod(thiz, methodId, env->NewStringUTF("System init play path"));
    //播放原音
    system->playSound(sound, 0, false, &channel);
    env->CallVoidMethod(thiz, methodId, env->NewStringUTF("System  play sound"));
    switch (model) {
        case com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_NORMAL:
            break;
        case com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_LUOLI:
            env->CallVoidMethod(thiz, methodId, env->NewStringUTF("萝莉"));
            system->createDSPByType(FMOD_DSP_TYPE_PITCHSHIFT, &dsp);
            dsp->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, 2.0);
            channel->addDSP(0, dsp);
            break;
        case com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_DASHU:
            env->CallVoidMethod(thiz, methodId, env->NewStringUTF("大叔"));
            system->createDSPByType(FMOD_DSP_TYPE_PITCHSHIFT, &dsp);
            dsp->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, 0.8);
            channel->addDSP(0, dsp);
            break;
        case com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_JINGSONG:
            env->CallVoidMethod(thiz, methodId, env->NewStringUTF("惊悚"));
            system->createDSPByType(FMOD_DSP_TYPE_ECHO, &dsp);
            dsp->setParameterFloat(FMOD_DSP_ECHO_DELAY, 800);
            dsp->setParameterFloat(FMOD_DSP_ECHO_FEEDBACK, 10);
            channel->addDSP(0, dsp);
            env->CallVoidMethod(thiz, methodId, env->NewStringUTF("惊悚添加第一种组合dsp"));
            system->createDSPByType(FMOD_DSP_TYPE_PITCHSHIFT, &dsp);
            dsp->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, 0.5);
            channel->addDSP(1, dsp);
            env->CallVoidMethod(thiz, methodId, env->NewStringUTF("惊悚添加第二种组合dsp"));
            env->CallVoidMethod(thiz, methodId, env->NewStringUTF("添加多种dsp,实现惊悚效果"));
            break;
        case com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_GAOGUAI:
            env->CallVoidMethod(thiz, methodId, env->NewStringUTF("搞怪"));
            float frequency;
            channel->getFrequency(&frequency);
            channel->setFrequency(frequency * 1.5f);
            env->CallVoidMethod(thiz, methodId, env->NewStringUTF("通过改变播放速度实现搞怪"));
            break;
        case com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_KONGLING:
            env->CallVoidMethod(thiz, methodId, env->NewStringUTF("空灵"));
            system->createDSPByType(FMOD_DSP_TYPE_ECHO, &dsp);
            dsp->setParameterFloat(FMOD_DSP_ECHO_DELAY, 200);
            dsp->setParameterFloat(FMOD_DSP_ECHO_FEEDBACK, 10);
            channel->addDSP(0, dsp);
            env->CallVoidMethod(thiz, methodId, env->NewStringUTF("通过设置回音等,实现空灵效果"));
            break;

    }
    bool isPlaying = true;
    env->CallVoidMethod(thiz, methodId, env->NewStringUTF("播放中..."));
    while (isPlaying && isResume) {
        channel->isPlaying(&isPlaying);
    }
    //释放资源
    env->CallVoidMethod(thiz, methodId, env->NewStringUTF("播放结束"));
    sound->release();
    env->CallVoidMethod(thiz, methodId, env->NewStringUTF("释放sound"));
    system->close();
    env->CallVoidMethod(thiz, methodId, env->NewStringUTF("关闭fmod"));
    system->release();
    env->CallVoidMethod(thiz, methodId, env->NewStringUTF("释放fmod"));
    env->ReleaseStringUTFChars(file_path_, 0);
    env->CallVoidMethod(thiz, methodId, env->NewStringUTF("释放路径"));
}

extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_activity_FmodActivity_stopPlayVoice(JNIEnv *env, jobject thiz) {
    // TODO: implement stopPlayVoice()
    jclass clazz = env->GetObjectClass(thiz);
    jmethodID methodId = env->GetMethodID(clazz, "onFomdCallback", "(Ljava/lang/String;)V");
    env->CallVoidMethod(thiz, methodId, env->NewStringUTF("停止播放,改变全局变量值"));
    isResume = false;
}
