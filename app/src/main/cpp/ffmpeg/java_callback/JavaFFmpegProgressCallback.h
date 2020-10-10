//
// Created by zenghui on 2020/9/24.
//

#ifndef JNISTUDY_JAVAFFMPEGPROGRESSCALLBACK_H
#define JNISTUDY_JAVAFFMPEGPROGRESSCALLBACK_H


#include "BaseJavaCallback.h"

class JavaFFmpegProgressCallback : public BaseJavaCallback {

public:
    JavaFFmpegProgressCallback(JavaVM *javaVm, JNIEnv *env, jobject &instance);

    virtual ~JavaFFmpegProgressCallback();

    void onProgress(int method, int duration);

private:
    jmethodID jmd;
};


#endif //JNISTUDY_JAVAFFMPEGPROGRESSCALLBACK_H
