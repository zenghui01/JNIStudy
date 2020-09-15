//
// Created by zenghui on 2020/3/17.
//

#include <jni.h>
#include <string>
#include <unistd.h>
#include "fmod.hpp"


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

extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_fmod_FmodUtils_nativePlayVoice(JNIEnv *env, jclass clazz, jint model,
                                                            jstring file_path_) {
    // TODO: implement nativePlayVoice()
    const char *file_path = env->GetStringUTFChars(file_path_, 0);
    System *system;
    Sound *sound;
    Channel *channel = 0;
    DSP *dsp = 0;
    //创建系统
    System_Create(&system);
    //初始化系统
    system->init(32, FMOD_INIT_NORMAL, 0);
    //创建声音
    system->createSound(file_path, FMOD_DEFAULT, 0, &sound);
    //播放原音
    system->playSound(sound, 0, false, &channel);
    switch (model) {
        case com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_NORMAL:
            break;
        case com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_LUOLI:
            system->createDSPByType(FMOD_DSP_TYPE_PITCHSHIFT, &dsp);
            dsp->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, 2.0);
            channel->addDSP(0, dsp);
            break;
        case com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_DASHU:
            system->createDSPByType(FMOD_DSP_TYPE_PITCHSHIFT, &dsp);
            dsp->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, 0.8);
            channel->addDSP(0, dsp);
            break;
        case com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_JINGSONG:
            system->createDSPByType(FMOD_DSP_TYPE_ECHO, &dsp);
            dsp->setParameterFloat(FMOD_DSP_ECHO_DELAY, 800);
            dsp->setParameterFloat(FMOD_DSP_ECHO_FEEDBACK, 10);
            channel->addDSP(0, dsp);

            system->createDSPByType(FMOD_DSP_TYPE_PITCHSHIFT, &dsp);
            dsp->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, 0.5);
            channel->addDSP(1, dsp);
            break;
        case com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_GAOGUAI:
            float frequency;
            channel->getFrequency(&frequency);
            channel->setFrequency(frequency * 1.5f);
            break;
        case com_testndk_jnistudy_ui_fmod_FmodUtils_MODEL_KONGLING:
            system->createDSPByType(FMOD_DSP_TYPE_ECHO, &dsp);
            dsp->setParameterFloat(FMOD_DSP_ECHO_DELAY, 200);
            dsp->setParameterFloat(FMOD_DSP_ECHO_FEEDBACK, 10);
            channel->addDSP(0, dsp);
            break;

    }
    bool isPlaying = true;
    while (isPlaying) {
        channel->isPlaying(&isPlaying);
        usleep(1000 * 1000);
    }

    //释放资源
    sound->release();
    system->close();
    system->release();

    env->ReleaseStringUTFChars(file_path_, 0);
}