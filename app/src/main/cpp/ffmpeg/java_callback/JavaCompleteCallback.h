//
// Created by zenghui on 2020/10/12.
//

#ifndef JNISTUDY_JAVACOMPLETECALLBACK_H
#define JNISTUDY_JAVACOMPLETECALLBACK_H


#include "BaseJavaCallback.h"

class JavaCompleteCallback : public BaseJavaCallback {
public:
    JavaCompleteCallback(JavaVM *javaVm, JNIEnv *env, const jobject &instance);

    virtual ~JavaCompleteCallback();

    void onComplete(int thread_mode);

    void onPlayPause(int thread_mode);

private:
    jmethodID jmd_complete;
    jmethodID jmd_pause;
};


#endif //JNISTUDY_JAVACOMPLETECALLBACK_H
