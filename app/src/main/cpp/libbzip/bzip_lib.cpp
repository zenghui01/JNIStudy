//
// Created by zenghui on 2020-02-21.
//
#include <jni.h>
#include <string>

extern "C" {

int bspatch_main(int argc, char *argv[]);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_testndk_jnistudy_ui_bsdiff_BsdiffUtilsJava_mergeApkNative(JNIEnv *env, jclass clazz,
                                                                   jstring old_apk, jstring new_apk,
                                                                   jstring path) {
    const char *oldApk = env->GetStringUTFChars(old_apk, 0);
    const char *newApk = env->GetStringUTFChars(new_apk, 0);
    const char *diffPath = env->GetStringUTFChars(path, 0);
    char *argv[] = {const_cast<char *>(""), const_cast<char *>(oldApk), const_cast<char *>(newApk),
                    const_cast<char *>(diffPath)};
    bspatch_main(4, argv);
    env->ReleaseStringUTFChars(old_apk, oldApk);
    env->ReleaseStringUTFChars(new_apk, newApk);
    env->ReleaseStringUTFChars(path, diffPath);
}