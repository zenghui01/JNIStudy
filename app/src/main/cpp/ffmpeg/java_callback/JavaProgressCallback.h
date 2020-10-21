//
// Created by zenghui on 2020/9/24.
//

#ifndef JNISTUDY_JAVAPROGRESSCALLBACK_H
#define JNISTUDY_JAVAPROGRESSCALLBACK_H


#include "BaseJavaCallback.h"

class JavaProgressCallback : public BaseJavaCallback {

public:
    JavaProgressCallback(JavaVM *javaVm, JNIEnv *env, jobject &instance);

    virtual ~JavaProgressCallback();

    void onProgress(int method, int duration);

private:
    jmethodID jmd;
};


#endif //JNISTUDY_JAVAPROGRESSCALLBACK_H
