#ifndef JNISTUDY_JAVAFFMPEGCALLBACK_H
#define JNISTUDY_JAVAFFMPEGCALLBACK_H

#include "BaseJavaCallback.h"

class JavaFFmpegCallback : BaseJavaCallback {
public:
    JavaFFmpegCallback(JavaVM *javaVm_, JNIEnv *env_, jobject instance_);

    virtual ~JavaFFmpegCallback();

    void prepare(int thread_mode);

    jmethodID jmd_prepared;

};


#endif //JNISTUDY_JAVAFFMPEGCALLBACK_H
