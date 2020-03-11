//
// Created by zenghui on 2020-02-19.
//
#include <jni.h>
#include <string>
#include "gif_lib.h"
/**
 * 数组指针
 *
 *
 * 指针数组
 *
 *
 */
#define  argb(a, r, g, b) ( ((a) & 0xff) << 24 ) | ( ((b) & 0xff) << 16 ) | ( ((g) & 0xff) << 8 ) | ((r) & 0xff)

typedef struct GifBean {
    //延迟时间
    int *delays;
    //当前帧
    int current_frame;
    //总帧数
    int total_frame;
} GifBean;

void drawFrame(GifFileType *gifFileType, GifBean *gifBean, AndroidBitmapInfo info, void *pixels);

extern "C"
JNIEXPORT jlong JNICALL
Java_com_testndk_jnistudy_ui_gif_GIfNativeDecoder_loadGifNative(JNIEnv *env, jclass clazz,
                                                                jstring path) {
    //转成路径指针
    const char *gif_path = (char *) env->GetStringUTFChars(path, 0);
    int error;
    //打开文件
    GifFileType *gifFileType = DGifOpenFileName(gif_path, &error);
    //初始化文件内容
    DGifSlurp(gifFileType);
    //分配内存给gifbean
    GifBean *gifbean = (GifBean *) malloc(sizeof(GifBean));
    //清理内存
    memset(gifbean, 0, sizeof(GifBean));
    //给延迟数组分配内存
    gifbean->delays = (int *) malloc(sizeof(int) * gifFileType->ImageCount);
    //清理数组内存
    memset(gifbean->delays, 0, sizeof(int) * gifFileType->ImageCount);
    ExtensionBlock *extensionBlock;
    //给gifbean赋值
    for (int i = 0; i < gifFileType->ImageCount; ++i) {
        SavedImage savedImage = gifFileType->SavedImages[i];
        for (int i = 0; i < savedImage.ExtensionBlockCount; ++i) {
            extensionBlock = &savedImage.ExtensionBlocks[i];
            if (extensionBlock->Function == GRAPHICS_EXT_FUNC_CODE) {
                break;
            }
        }
        if (extensionBlock) {
            gifbean->delays[i] = (extensionBlock->Bytes[2] << 8 | extensionBlock->Bytes[1]) * 10;
        }
    }
    gifbean->total_frame = gifFileType->ImageCount;
    gifFileType->UserData = gifbean;
    env->ReleaseStringUTFChars(path, gif_path);
    return (jlong) gifFileType;
}

void drawFrame(GifFileType *gifFileType, GifBean *gifBean, AndroidBitmapInfo info, void *pixels) {
    //获取当前帧存储图像
    SavedImage savedImage = gifFileType->SavedImages[gifBean->current_frame];
    //获取图像的描述
    GifImageDesc imageDesc = savedImage.ImageDesc;
    //获取颜色对照表
    ColorMapObject *colorMapObject = imageDesc.ColorMap;
    if (colorMapObject == NULL) {
        colorMapObject = gifFileType->SColorMap;
    }
    //偏移指针
    int *px = (int *) pixels;
    //偏移指针地址
    px = (int *) ((char *) px + info.stride * imageDesc.Top);

    int *line;//每一行的首地址
    int pointPixelsIndex;//像素点的索引值
    GifByteType byteType;//图像颜色索引
    GifColorType colorType;//图像颜色值
    for (int y = imageDesc.Top; y < imageDesc.Top + imageDesc.Height; ++y) {
        line = px;
        for (int x = imageDesc.Left; x < imageDesc.Left + imageDesc.Width; ++x) {
            pointPixelsIndex = (y - imageDesc.Top) * imageDesc.Width + (x - imageDesc.Left);
            byteType = savedImage.RasterBits[pointPixelsIndex];
            if (colorMapObject != NULL) {
                colorType = colorMapObject->Colors[byteType];
                line[x] = argb(255, colorType.Red, colorType.Green, colorType.Blue);
            }
        }
        px = (int *) ((char *) px + info.stride);
    }

}

extern "C"
JNIEXPORT jint JNICALL
Java_com_testndk_jnistudy_ui_gif_GIfNativeDecoder_getWidth(JNIEnv *env, jclass clazz,
                                                           jlong gif_point) {
    GifFileType *gifFileType = (GifFileType *) gif_point;
    return gifFileType->SWidth;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_testndk_jnistudy_ui_gif_GIfNativeDecoder_getHeight(JNIEnv *env, jclass clazz,
                                                            jlong gif_point) {
    GifFileType *gifFileType = (GifFileType *) gif_point;
    return gifFileType->SHeight;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_testndk_jnistudy_ui_gif_GIfNativeDecoder_updateFrame(JNIEnv *env, jclass clazz,
                                                              jobject bitmap, jlong gif_point) {
    //获取gif完整信息
    GifFileType *gifFileType = (GifFileType *) gif_point;
    //获取gifbean的数据
    GifBean *gifBean = (GifBean *) gifFileType->UserData;
    //bitmap jni层信息
    AndroidBitmapInfo info;
    //将java 层bitmap信息转换为jni层信息
    AndroidBitmap_getInfo(env, bitmap, &info);
    void *pixel;
    AndroidBitmap_lockPixels(env, bitmap, &pixel);

    drawFrame(gifFileType, gifBean, info, pixel);

    gifBean->current_frame += 1;

    if (gifBean->current_frame >= gifBean->total_frame) {
        gifBean->current_frame = 0;
    }
    AndroidBitmap_unlockPixels(env, bitmap);

    return gifBean->delays[gifBean->current_frame];
}