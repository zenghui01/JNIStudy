//package com.testndk.jnistudy.ui.rtmp;
//
//import android.app.Activity;
//import android.hardware.Camera;
//import android.view.SurfaceHolder;
//
//public class VideoChannel implements CameraHelper.OnChangedSizeListener, Camera.PreviewCallback {
//    CameraHelper helper;
//    public static int mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
//    int mCameraWidth = 480;
//    int mCameraHeight = 800;
//    int bitrate = 800000;
//    int fps = 25;
//    Pusher mPusher;
//    boolean isPlaying = false;
//
//    public VideoChannel(Activity mActivity, Pusher pusher) {
//        mPusher = pusher;
//        helper = new CameraHelper(mActivity, mCameraID, mCameraWidth, mCameraHeight);
//        helper.setOnChangedSizeListener(this);
//        helper.setPreviewCallback(this);
//    }
//
//    public void setPreviewDisplay(SurfaceHolder surfaceHolder){
//        helper.setPreviewDisplay(surfaceHolder);
//    }
//
//
//    public void startLive() {
//        isPlaying=true;
//    }
//
//    public void stopLive() {
//        isPlaying=false;
//    }
//
//    public void switchCamera() {
//        helper.switchCamera();
//    }
//
//    @Override
//    public void onChanged(int width, int height) {
//        mPusher.initVideoEncoderNative(width, height, bitrate, fps);
//    }
//
//    @Override
//    public void onPreviewFrame(byte[] data, Camera camera) {
//        if(isPlaying) {
//            mPusher.pushCameraFrame(data);
//        }
//    }
//
//    public void release() {
//    }
//}
