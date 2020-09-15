//
// Created by zenghui on 2020/9/14.
//

#include "BaseJavaCallback.h"

BaseJavaCallback::BaseJavaCallback(JavaVM *javaVm_, JNIEnv *env_, jobject instance_) {
    this->javaVM = javaVm_;
    //env不能跨线程,我认为env是当前线程环境,当切换了线程,那么当前线程的env是不能在其他线程使用的
    this->env = env_;
    //jstring jobject一旦涉及到跨线程就需要创建全局引用
    this->instance = env->NewGlobalRef(instance_);
    clazz = env->GetObjectClass(this->instance);
}

BaseJavaCallback::~BaseJavaCallback() {
    javaVM = 0;
    env->DeleteLocalRef(instance);
    instance = 0;
    env = 0;
}

