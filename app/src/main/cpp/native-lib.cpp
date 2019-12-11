#include <jni.h>
#include <string>

#include "art_5_1.h"


extern "C" JNIEXPORT jstring JNICALL
Java_com_testndk_jnistudy_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_andfix_DexManager_replace(JNIEnv *env, jobject thiz,
                                                       jobject fix_method, jobject bug_method) {

}