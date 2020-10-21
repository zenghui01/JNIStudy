//
// Created by zenghui on 2020/9/16.
//
#include <jni.h>
#include "../macro.h"

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_testndk_jnistudy_ui_ffmpeg_FFmpegPlayer_checkSign(JNIEnv *env, jclass clazz,
                                                           jobject context_) {
    //获取到context类对象
    jclass context = env->GetObjectClass(context_);

    //从context中获取getPackageManager方法id
    jmethodID getPackageManagerMethod = env->GetMethodID(context, "getPackageManager",
                                                         "()Landroid/content/pm/PackageManager;");
    //根据getPackageManager方法id获取到PackageManager对应的obj
    jobject packageManagerObj = env->CallObjectMethod(context_, getPackageManagerMethod);

    //根据PackageManager的obj获取到PackageManager类
    jclass packageClass = env->GetObjectClass(packageManagerObj);

    //从context中获取getPackageNameMethod的方法id
    jmethodID getPackageNameMethod = env->GetMethodID(context, "getPackageName",
                                                      "()Ljava/lang/String;");
    //从PackageManager中获取getPackageNameMethod的方法id
    jmethodID getPackageInfoMethod = env->GetMethodID(packageClass, "getPackageInfo",
                                                      "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");

    //从context中根据getPackageNameMethod获取到包名(jString)
    auto jPackageName = (jstring) env->CallObjectMethod(context_, getPackageNameMethod);

    //获取包名char字符串
    const char *packageNameChar = env->GetStringUTFChars(jPackageName, 0);
    LOGE("包名:%s", packageNameChar);

    //从PackageManager中GET_SIGNATURES字段id
    jfieldID flags = env->GetStaticFieldID(packageClass, "GET_SIGNATURES", "I");

    //从PackageManager中GET_SIGNATURES字段id值
    int flagsValue = env->GetStaticIntField(packageClass, flags);

    //调用PackageManager中getPackageInfo方法,返回packageInfo对应的obj
    jobject packageInfoObj = env->CallObjectMethod(packageManagerObj, getPackageInfoMethod,
                                                   jPackageName, flagsValue);
    if (packageInfoObj == nullptr) {
        return 0;
    }
    //根据packageInfoObj获取packageInfoClass
    jclass packageInfoClass = env->GetObjectClass(packageInfoObj);
    //从packageInfo类中获取signatures字段id
    jfieldID signaturesFieldId = env->GetFieldID(packageInfoClass, "signatures",
                                                 "[Landroid/content/pm/Signature;");
    //根据signatures字段id获取到数组
    auto signaturesArr = (jobjectArray) env->GetObjectField(packageInfoObj, signaturesFieldId);

    jobject signatureObj = env->GetObjectArrayElement(signaturesArr, 0);

    jclass signatureClass = env->GetObjectClass(signatureObj);

    auto signByteArr = (jbyteArray) env->GetMethodID(signatureClass, "toByteArray", "()[B");

    jclass byteInputStreamClass = env->FindClass("java/io/ByteArrayInputStream");

    jmethodID byteInputStreamInitMethodId = env->GetMethodID(byteInputStreamClass, "<init>",
                                                             "([B)V");

    jobject byteInputStreamObj = env->NewObject(byteInputStreamClass, byteInputStreamInitMethodId,
                                                signByteArr);

    jclass certificateFactoryClass = env->FindClass("java/security/cert/CertificateFactory");

    jmethodID certificateInstanceMethod = env->GetStaticMethodID(certificateFactoryClass,
                                                                 "getInstance",
                                                                 "(Ljava/lang/String;)Ljava/security/cert/CertificateFactory;");

    jmethodID generateCertificateMethod = env->GetMethodID(certificateFactoryClass,
                                                           "generateCertificate",
                                                           "(Ljava/io/InputStream;)Ljava/security/cert/Certificate;");
    const char *typeStr = "X509";
    jstring encodeType = env->NewStringUTF(typeStr);

    jobject certificateFactoryObj = env->CallStaticObjectMethod(certificateFactoryClass,
                                                                certificateInstanceMethod,
                                                                encodeType);

    jobject x509CertificateObj = env->CallObjectMethod(certificateFactoryObj,
                                                       generateCertificateMethod,
                                                       byteInputStreamObj);

    jclass messageDigestClass = env->FindClass("java/security/MessageDigest");

    env->ReleaseStringUTFChars(encodeType, typeStr);
    env->DeleteLocalRef(certificateFactoryObj);
    env->ReleaseStringUTFChars(jPackageName, packageNameChar);
    env->DeleteLocalRef(jPackageName);
    env->DeleteLocalRef(byteInputStreamObj);
    env->DeleteLocalRef(signatureObj);
    env->DeleteLocalRef(packageInfoObj);
    env->DeleteLocalRef(packageManagerObj);
    env->DeleteLocalRef(context_);
    return 0;
}
