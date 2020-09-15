#include <jni.h>
#include <string>
#include "FFmpegPlayer.h"

extern "C" {
#include <libavcodec/avcodec.h>
}


JavaVM *javaVM = 0;

jint JNI_OnLoad(JavaVM *vm, void *args) {
    javaVM = vm;
    return JNI_VERSION_1_6;
}

extern "C"

JNIEXPORT jstring JNICALL
Java_com_testndk_jnistudy_ui_ffmpeg_FFmpegPlayer_getVersion(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(av_version_info());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_ffmpeg_FFmpegPlayer_prepareNative(JNIEnv *env, jobject thiz_,
                                                               jstring data_source_) {
    const char *data_source = env->GetStringUTFChars(data_source_, 0);
    LOGE("视频地址:%s", data_source);
    auto *ffCallBack = new JavaFFmpegCallback(javaVM, env, thiz_);
    auto *ffErrorCallback = new JavaFFmpegErrorCallback(javaVM, env, thiz_);
    auto *player = new FFmpegPlayer("data_source");
    player->setFFmpegCallback(ffCallBack);
    player->setFFmpegErrorCallback(ffErrorCallback);
    player->prepare();

}

extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_ffmpeg_FFmpegPlayer_startNative(JNIEnv *env, jobject thiz) {

}

extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_ffmpeg_FFmpegPlayer_stopNative(JNIEnv *env, jobject thiz) {

}

extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_ffmpeg_FFmpegPlayer_releaseNative(JNIEnv *env, jobject thiz) {

}