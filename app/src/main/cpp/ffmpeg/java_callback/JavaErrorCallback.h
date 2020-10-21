#ifndef JNISTUDY_JAVAERRORCALLBACK_H
#define JNISTUDY_JAVAERRORCALLBACK_H

#include "BaseJavaCallback.h"
#include <cstring>

class JavaErrorCallback : public BaseJavaCallback {
public:
    JavaErrorCallback(JavaVM *javaVM_, JNIEnv *env_, jobject instance_);

    virtual ~JavaErrorCallback();

    void onError(int method, jint code, const char *str);

private:
    jmethodID jmd_test;
    jmethodID jmd_jni_error;
    jmethodID jmd_test_boolean;
    jmethodID jmd_test_return;
};

#endif //JNISTUDY_JAVAERRORCALLBACK_H
