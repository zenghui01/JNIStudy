package com.testndk.jnistudy.ui.gif;

import android.graphics.Bitmap;

public class GIfNativeDecoder {
    static {
        System.loadLibrary("gif-lib");
    }

    private long gifPoint;

    public GIfNativeDecoder(long gifPoint) {
        this.gifPoint = gifPoint;
    }

    public static GIfNativeDecoder loadFile(String path) {
        long gifFileTypePoint = loadGifNative(path);
        return new GIfNativeDecoder(gifFileTypePoint);
    }

    public long getGifPoint() {
        return gifPoint;
    }

    /**
     * 加载gif文件
     *
     * @param path 图片路径
     * @return 返回指针地址
     */
    public static native long loadGifNative(String path);

    public static native int getWidth(long gifPoint);

    public static native int getHeight(long gifPoint);

    /**
     * 刷新每一帧,返回延迟时间
     * @param bitmap
     * @param gifPoint
     * @return
     */
    public static native int updateFrame(Bitmap bitmap, long gifPoint);
}
