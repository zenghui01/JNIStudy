//
// Created by zenghui on 2020/9/14.
//

#ifndef JNISTUDY_BASEJAVACALLBACK_H
#define JNISTUDY_BASEJAVACALLBACK_H

#include "../macro.h"
#include <jni.h>

class BaseJavaCallback {
public:
    BaseJavaCallback(JavaVM *javaVM, JNIEnv *env, jobject instance_);

    ~BaseJavaCallback();

    JavaVM *javaVM = 0;
    JNIEnv *env = 0;
    jobject instance;
    jclass clazz;

};


#endif //JNISTUDY_BASEJAVACALLBACK_H
