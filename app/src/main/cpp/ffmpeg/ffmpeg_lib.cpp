#include <jni.h>
#include <string>
#include "FFmpegPlayer.h"
#include <android/native_window_jni.h>


JavaVM *javaVM = 0;
FFmpegPlayer *player;
ANativeWindow *window = 0;
pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;

jint JNI_OnLoad(JavaVM *vm, void *args) {
    javaVM = vm;
    return JNI_VERSION_1_6;
}

extern "C"

JNIEXPORT jstring JNICALL
Java_com_testndk_jnistudy_ui_ffmpeg_FFmpegPlayer_getVersion(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(av_version_info());
}


void renderFrame(uint8_t *src_data, int w, int h, int src_lineSize) {
    pthread_mutex_lock(&mutex);
    if (!window) {
        // todo 窗口初始化失败????
        player->stop();
        pthread_mutex_unlock(&mutex);
        return;
    }
    //设置窗口属性
    ANativeWindow_setBuffersGeometry(window, w,
                                     h,
                                     WINDOW_FORMAT_RGBA_8888);

    ANativeWindow_Buffer window_buffer;
    if (ANativeWindow_lock(window, &window_buffer, 0)) {
        ANativeWindow_release(window);
        window = 0;
        pthread_mutex_unlock(&mutex);
        return;
    }

    //填充buffer
    uint8_t *dst_data = static_cast<uint8_t *>(window_buffer.bits);
    int dst_lineSize = window_buffer.stride * 4;//RGBA
    for (int i = 0; i < window_buffer.height; ++i) {
        //一行拷贝
        memcpy(dst_data + i * dst_lineSize, src_data + i * src_lineSize, dst_lineSize);
    }
    ANativeWindow_unlockAndPost(window);
    pthread_mutex_unlock(&mutex);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_ffmpeg_FFmpegPlayer_prepareNative(JNIEnv *env, jobject thiz_,
                                                               jstring data_source_) {
    const char *data_source = env->GetStringUTFChars(data_source_, 0);
    LOGE("视频地址:%s", data_source);
    auto *ffCallBack = new JavaFFmpegCallback(javaVM, env, thiz_);
    auto *ffErrorCallback = new JavaFFmpegErrorCallback(javaVM, env, thiz_);
    auto *ffProgressCallback = new JavaFFmpegProgressCallback(javaVM, env, thiz_);
    player = new FFmpegPlayer(data_source);
    player->setFFmpegCallback(ffCallBack);
    player->setFFmpegErrorCallback(ffErrorCallback);
    player->setProgressCallback(ffProgressCallback);
    player->setRenderCallback(renderFrame);
    player->prepare();
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_testndk_jnistudy_ui_ffmpeg_FFmpegPlayer_getDurationNative(JNIEnv *env, jobject thiz) {
    if (!player) {
        return -1;
    }
    return player->getDuration();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_ffmpeg_FFmpegPlayer_startNative(JNIEnv *env, jobject thiz) {
    if (player) {
        LOGE("开始播放");
        player->start();
    }
}


extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_ffmpeg_FFmpegPlayer_stopNative(JNIEnv *env, jobject thiz) {

}

extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_ffmpeg_FFmpegPlayer_releaseNative(JNIEnv *env, jobject thiz) {

}

extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_ffmpeg_FFmpegPlayer_setSurfaceNative(JNIEnv *env, jobject thiz,
                                                                  jobject surface) {
    LOGE("初始化window");
    pthread_mutex_lock(&mutex);
    if (window) {
        ANativeWindow_release(window);
        window = 0;
    }
    window = ANativeWindow_fromSurface(env, surface);
    pthread_mutex_unlock(&mutex);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_ffmpeg_FFmpegPlayer_onSeekNative(JNIEnv *env, jobject thiz,
                                                              jint duration) {
    if (player) {
        player->onSeek(duration);
    }
}