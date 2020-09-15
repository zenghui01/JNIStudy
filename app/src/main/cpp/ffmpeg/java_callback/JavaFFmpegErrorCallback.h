#ifndef JNISTUDY_JAVAFFMPEGERRORCALLBACK_H
#define JNISTUDY_JAVAFFMPEGERRORCALLBACK_H

#include "BaseJavaCallback.h"
#include <cstring>

class JavaFFmpegErrorCallback : public BaseJavaCallback {
public:
    JavaFFmpegErrorCallback(JavaVM *javaVM_, JNIEnv *env_, jobject instance_);

    virtual ~JavaFFmpegErrorCallback();

    void onError(jint code, const char *str);

private:
    jmethodID jmd_test;
    jmethodID jmd_jni_error;
    jmethodID jmd_test_boolean;
    jmethodID jmd_test_return;
};

#endif //JNISTUDY_JAVAFFMPEGERRORCALLBACK_H
