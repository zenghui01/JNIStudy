package com.testndk.jnistudy.ui.rtmp;

import android.app.Activity;
import android.view.SurfaceHolder;

public class Pusher {
    static {
        System.loadLibrary("rtmp_push");
    }

    AudioChannel mAudioChannel;
    VideoChannel mVideoChannel;

    public Pusher(Activity activity) {
        initRTMP();
        mAudioChannel = new AudioChannel(this);
        mVideoChannel = new VideoChannel(activity, this);
    }

    public void setPreviewDisplay(SurfaceHolder surfaceHolder) {
        mVideoChannel.setPreviewDisplay(surfaceHolder);
    }

    public void switchCamera() {
        mVideoChannel.switchCamera();
    }

    public void startLive(String liveUrl) {
        startNativeLive(liveUrl);
        mAudioChannel.startLive();
        mVideoChannel.startLive();
    }


    public void stopLive() {
        mAudioChannel.stopLive();
        mVideoChannel.stopLive();
    }

    public void release() {
        mAudioChannel.release();
        mVideoChannel.release();
    }

    /**
     * 初始化rtmp
     */
    private native void initRTMP();

    /**
     * 初始化直播
     *
     * @param live_url
     */
    private native void startNativeLive(String live_url);
    //-------------------- 视频推流模块 -----------------------------

    /**
     * 初始化视频编码器
     */
    public native void initVideoEncoderNative(int width, int height, int bitrate, int fps);

    /**
     * 推送视频数据
     *
     * @param data 视频数据
     */
    public native void pushCameraFrame(byte[] data);

    //-------------------- 视频推流模块 -----------------------------


    //--------------------- 音频推流 ------------------------------

    /**
     * 初始化音频编码器
     *
     * @param sampleRateInHz 采样率
     * @param channelConfig  音频通道的配置。
     */
    public native void initAudioEncoderNative(int sampleRateInHz, int channelConfig);

    /**
     * 获取编译器输入样本数
     *
     * @return
     */
    public native int getInputSimplesNative();

    /**
     * 推送音频数据
     *
     * @param data 音频数据
     */
    public native void pushAudioData(byte[] data);

    //--------------------- 音频推流 ------------------------------
    private native void stopRTMPNative();

    private native void releaseRTMPNatice();

}
