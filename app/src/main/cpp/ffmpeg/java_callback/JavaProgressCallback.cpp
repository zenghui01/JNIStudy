//
// Created by zenghui on 2020/9/24.
//

#include "JavaProgressCallback.h"

JavaProgressCallback::JavaProgressCallback(JavaVM *javaVm, JNIEnv *env,
                                           jobject &instance) :
        BaseJavaCallback(javaVm, env, instance) {
    jmd = env->GetMethodID(clazz, "onJniProgress", "(I)V");
}

JavaProgressCallback::~JavaProgressCallback() {

}

void JavaProgressCallback::onProgress(int method, int duration) {
    if (method == THREAD_MAIN) {
        env->CallVoidMethod(instance, jmd, duration);
    } else if (method == THREAD_CHILD) {
        JNIEnv *child_env;

        javaVM->AttachCurrentThread(&child_env, 0);

        child_env->CallVoidMethod(instance, jmd, duration);

        javaVM->DetachCurrentThread();

        child_env = 0;
    }
}
