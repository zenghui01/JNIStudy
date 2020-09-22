#include "JavaFFmpegErrorCallback.h"

void JavaFFmpegErrorCallback::onError(int method, jint code, const char *str) {
    if (method == THREAD_MAIN) {
        jstring data = env->NewStringUTF(str);

        env->CallVoidMethod(instance, jmd_test, data);

        env->CallVoidMethod(instance, jmd_jni_error, code, data);

        env->CallVoidMethod(instance, jmd_test_boolean, false, data);

        env->CallIntMethod(instance, jmd_test_return, code, data);
    } else if (method == THREAD_CHILD) {
        JNIEnv *child_env;

        javaVM->AttachCurrentThread(&child_env, 0);

        jstring data = child_env->NewStringUTF(str);

        child_env->CallVoidMethod(instance, jmd_test, data);

        child_env->CallVoidMethod(instance, jmd_jni_error, code, data);

        child_env->CallVoidMethod(instance, jmd_test_boolean, false, data);

        child_env->CallIntMethod(instance, jmd_test_return, code, data);

        javaVM->DetachCurrentThread();
    }
}

JavaFFmpegErrorCallback::~JavaFFmpegErrorCallback() {

}

JavaFFmpegErrorCallback::JavaFFmpegErrorCallback(JavaVM *javaVM_, JNIEnv *env_, jobject instance_)
        : BaseJavaCallback(javaVM_, env_, instance_) {
    jmd_test = env->GetMethodID(clazz, "onTest", "(Ljava/lang/String;)V");
    jmd_jni_error = env->GetMethodID(clazz, "onJniError", "(ILjava/lang/String;)V");
    jmd_test_boolean = env->GetMethodID(clazz, "onTestBoolean", "(ZLjava/lang/String;)V");
    jmd_test_return = env->GetMethodID(clazz, "onTestReturn", "(ILjava/lang/String;)I");
}

