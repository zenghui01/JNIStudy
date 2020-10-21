//
// Created by zenghui on 2020/10/12.
//

#include "JavaCompleteCallback.h"

JavaCompleteCallback::JavaCompleteCallback(JavaVM *javaVm, JNIEnv *env, const jobject &instance)
        : BaseJavaCallback(javaVm, env, instance) {
    jmd_complete = env->GetMethodID(clazz, "onPlayComplete", "()V");
    jmd_pause = env->GetMethodID(clazz, "onPlayPause", "()V");
}

JavaCompleteCallback::~JavaCompleteCallback() {

}

void JavaCompleteCallback::onPlayPause(int thread_mode) {
    if (thread_mode == THREAD_MAIN) {
        env->CallVoidMethod(instance, jmd_pause);
    } else if (thread_mode == THREAD_CHILD) {
        JNIEnv *child_env;

        javaVM->AttachCurrentThread(&child_env, 0);

        child_env->CallVoidMethod(instance, jmd_pause);

        javaVM->DetachCurrentThread();
    }
}

void JavaCompleteCallback::onComplete(int thread_mode) {
    if (thread_mode == THREAD_MAIN) {
        env->CallVoidMethod(instance, jmd_complete);
    } else if (thread_mode == THREAD_CHILD) {
        JNIEnv *child_env;

        javaVM->AttachCurrentThread(&child_env, 0);

        child_env->CallVoidMethod(instance, jmd_complete);

        javaVM->DetachCurrentThread();
    }
}
