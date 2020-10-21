#include <jni.h>
#include <string>

#include "art_5_1.h"
//#include "art_4_4.h"

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
    art::mirror::ArtMethod *dmeth = reinterpret_cast<art::mirror::ArtMethod *>(env->FromReflectedMethod(
            fix_method));
    art::mirror::ArtMethod *smeth = reinterpret_cast<art::mirror::ArtMethod *>(env->FromReflectedMethod(
            bug_method));
    reinterpret_cast<art::mirror::Class *>(dmeth->declaring_class_)->class_loader_ =
            reinterpret_cast<art::mirror::Class *>(smeth->declaring_class_)->class_loader_; //for plugin classloader
    reinterpret_cast<art::mirror::Class *>(dmeth->declaring_class_)->clinit_thread_id_ =
            reinterpret_cast<art::mirror::Class *>(smeth->declaring_class_)->clinit_thread_id_;
    reinterpret_cast<art::mirror::Class *>(dmeth->declaring_class_)->status_ =
            reinterpret_cast<art::mirror::Class *>(smeth->declaring_class_)->status_ - 1;
    //for reflection invoke
    reinterpret_cast<art::mirror::Class *>(dmeth->declaring_class_)->super_class_ = 0;

    smeth->declaring_class_ = dmeth->declaring_class_;
    smeth->dex_cache_resolved_types_ = dmeth->dex_cache_resolved_types_;
    smeth->access_flags_ = dmeth->access_flags_ | 0x0001;
    smeth->dex_cache_resolved_methods_ = dmeth->dex_cache_resolved_methods_;
    smeth->dex_code_item_offset_ = dmeth->dex_code_item_offset_;
    smeth->method_index_ = dmeth->method_index_;
    smeth->dex_method_index_ = dmeth->dex_method_index_;

    smeth->ptr_sized_fields_.entry_point_from_interpreter_ =
            dmeth->ptr_sized_fields_.entry_point_from_interpreter_;

    smeth->ptr_sized_fields_.entry_point_from_jni_ =
            dmeth->ptr_sized_fields_.entry_point_from_jni_;
    smeth->ptr_sized_fields_.entry_point_from_quick_compiled_code_ =
            dmeth->ptr_sized_fields_.entry_point_from_quick_compiled_code_;

}
