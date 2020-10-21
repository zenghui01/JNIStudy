#ifndef JNISTUDY_JAVACALLBACK_H
#define JNISTUDY_JAVACALLBACK_H

#include "BaseJavaCallback.h"

class JavaCallback : BaseJavaCallback {
public:
    JavaCallback(JavaVM *javaVm_, JNIEnv *env_, jobject instance_);

    virtual ~JavaCallback();

    void prepare(int thread_mode);

    jmethodID jmd_prepared;

};


#endif //JNISTUDY_JAVACALLBACK_H
