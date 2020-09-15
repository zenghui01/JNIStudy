//
// Created by zenghui on 2020/9/13.
//

#include<android/log.h>

#ifndef JNISTUDY_MACRO_H
#define JNISTUDY_MACRO_H

#define THREAD_MAIN 1

#define THREAD_CHILD 2

#define  LOG_TAG    "error_log jni"

#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#endif //JNISTUDY_MACRO_H
