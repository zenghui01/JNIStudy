#include "JavaCallback.h"


JavaCallback::~JavaCallback() {

}


void JavaCallback::prepare(int thread_mode) {
    if (thread_mode == THREAD_MAIN) {
        env->CallVoidMethod(instance, jmd_prepared);
    } else if (thread_mode == THREAD_CHILD) {
        //如果涉及到线程切换,需要重新获取线程env环境
        JNIEnv *child_env;
        javaVM->AttachCurrentThread(&child_env, 0);
        child_env->CallVoidMethod(instance, jmd_prepared);
        javaVM->DetachCurrentThread();
    }
}

JavaCallback::JavaCallback(JavaVM *javaVm_, JNIEnv *env_, jobject instance_)
        : BaseJavaCallback(javaVm_, env_, instance_) {
    jmd_prepared = env->GetMethodID(clazz, "onJniPrepared", "()V");
}






