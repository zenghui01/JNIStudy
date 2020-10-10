package com.testndk.jnistudy.ui.ffmpeg;


import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.testndk.jnistudy.utils.LogUtils;

public class FFmpegPlayer implements SurfaceHolder.Callback {
    static {
        System.loadLibrary("ffmpeg_lib");
    }

    public native static String getVersion();

    private OnPrepareListener mPrepareListener;

    private OnProgressListener mProgressListener;

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

    public void setProgressListener(OnProgressListener mProgressListener) {
        this.mProgressListener = mProgressListener;
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

    public int getDuration() {
        return getDurationNative();
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

    public void onSeek(int duration) {
        onSeekNative(duration);
    }

    /**
     * jni反射调用
     */
    private void onJniPrepared() {
        if (null != mPrepareListener) {
            mPrepareListener.onPrepare(getDuration());
        }
    }

    private void onJniProgress(int duration) {
        LogUtils.eLog("onJniProgress", duration);
        if (null != mProgressListener) {
            mProgressListener.onProgress(duration);
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
        setSurfaceNative(holder.getSurface());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogUtils.eLog("surfaceDestroyed");
    }

    public interface OnPrepareListener {
        void onPrepare(int duration);
    }

    public interface OnErrorListener {
        void onError(int errorCode, String errorMsg);
    }

    public interface OnProgressListener {
        void onProgress(int progress);
    }

    private native void prepareNative(String dataSource);

    private native void startNative();

    private native void stopNative();

    private native void releaseNative();

    private native void setSurfaceNative(Surface surface);

    private native void onSeekNative(int duration);

    private native int getDurationNative();
}
