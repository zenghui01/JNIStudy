package com.testndk.jnistudy.ui.ffmpeg;


import com.testndk.jnistudy.utils.LogUtils;

public class FFmpegPlayer {
    static {
        System.loadLibrary("ffmpeg_lib");
    }

    public native static String getVersion();

    private OnPrepareListener mPrepareListener;

    private OnErrorListener mErrorListener;

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

    private int onTestReturn(int errorCode, String errorMsg ){
        LogUtils.eLog("测试jni回调java", "onTestReturn", errorCode, errorMsg);
        return errorCode;
    }

    /**
     * jni反射调用
     */
    private void onJniPrepared() {
        if (null != mPrepareListener) {
            mPrepareListener.onPrepare();
        }
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
}
