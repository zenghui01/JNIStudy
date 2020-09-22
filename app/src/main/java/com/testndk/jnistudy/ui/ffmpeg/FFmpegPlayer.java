package com.testndk.jnistudy.ui.ffmpeg;


import android.content.Context;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.testndk.jnistudy.utils.LogUtils;
import com.testndk.jnistudy.utils.ScreenUtil;

public class FFmpegPlayer implements SurfaceHolder.Callback {
    static {
        System.loadLibrary("ffmpeg_lib");
//        System.loadLibrary("apkSign-lib");
    }

    public native static String getVersion();

    private OnPrepareListener mPrepareListener;

    private OnErrorListener mErrorListener;

    public SurfaceHolder surfaceHolder;


    public FFmpegPlayer() {
    }

    //播放源,本地视频或者是直播流
    private String dataSource;

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }


    public void setPrepareListener(OnPrepareListener mPrepareListener) {
        this.mPrepareListener = mPrepareListener;
    }

    public void setErrorListener(OnErrorListener mErrorListener) {
        this.mErrorListener = mErrorListener;
    }

    public void prepare() {
        prepareNative(dataSource);
    }

    public void start() {
        startNative();
    }

    public void stop() {

    }

    public void release() {

    }

    private void onJniError(int errorCode, String errorMsg) {
        LogUtils.eLog("测试jni回调java", "onJniError", errorCode, errorMsg);
        if (null != mErrorListener) {
            mErrorListener.onError(0, errorMsg);
        }
    }

    private void onTestBoolean(boolean b, String errorMsg) {
        LogUtils.eLog("测试jni回调java", "onTestBoolean", b, errorMsg);
    }

    private void onTest(String errorMsg) {
        LogUtils.eLog("测试jni回调java", "onTest", errorMsg);
    }

    private int onTestReturn(int errorCode, String errorMsg) {
        LogUtils.eLog("测试jni回调java", "onTestReturn", errorCode, errorMsg);
        return errorCode;
    }

//    public static native boolean checkSign(Context context);

    /**
     * jni反射调用
     */
    private void onJniPrepared() {
        if (null != mPrepareListener) {
            mPrepareListener.onPrepare();
        }
    }


    public void setSurface(SurfaceView surface) {
        if (surfaceHolder != null) {
            surfaceHolder.removeCallback(this);
        }
        surfaceHolder = surface.getHolder();
        LogUtils.eLog("setSurface");
        // TODO: 2020/9/21 不回调 surfaceChanged???
        setSurfaceNative(surfaceHolder.getSurface());
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LogUtils.eLog("surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        LogUtils.eLog("surfaceChanged");
        setSurfaceNative(holder.getSurface());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogUtils.eLog("surfaceDestroyed");
    }

    public interface OnPrepareListener {
        void onPrepare();
    }

    public interface OnErrorListener {
        void onError(int errorCode, String errorMsg);
    }

    private native void prepareNative(String dataSource);

    private native void startNative();

    private native void stopNative();

    private native void releaseNative();

    private native void setSurfaceNative(Surface surface);
}
