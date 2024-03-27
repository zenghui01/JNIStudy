//package com.testndk.jnistudy.ui.opengl.two;
//
//import android.graphics.SurfaceTexture;
//import android.opengl.GLSurfaceView;
//
//import androidx.camera.core.Preview;
//import androidx.lifecycle.LifecycleOwner;
//
//import javax.microedition.khronos.egl.EGLConfig;
//import javax.microedition.khronos.opengles.GL10;
//
//public class CameraRender implements GLSurfaceView.Renderer, Preview.OnPreviewOutputUpdateListener, SurfaceTexture.OnFrameAvailableListener {
//    private static final String TAG = "david";
//    private CameraView cameraView;
//    private SurfaceTexture mCameraTexture;
//    private ScreenFilter screenFilter;
//    private int[] textures;
//    float[] mtx = new float[16];
//
//    public CameraRender(CameraView cameraView) {
//        this.cameraView = cameraView;
//        LifecycleOwner lifecycleOwner = (LifecycleOwner) cameraView.getContext();
//        // 打开摄像头
//        new CameraHelper(lifecycleOwner, this);
//    }
//
//    @Override
//    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//        textures = new int[1];
//        // 设置共同源
//        mCameraTexture.attachToGLContext(textures[0]);
//        // 监听摄像头数据回调，
//        mCameraTexture.setOnFrameAvailableListener(this);
//        screenFilter = new ScreenFilter(cameraView.getContext());
//    }
//
//    @Override
//    public void onSurfaceChanged(GL10 gl, int width, int height) {
//        screenFilter.setSize(width, height);
//    }
//
//    @Override
//    public void onDrawFrame(GL10 gl) {
//        // 将摄像头的数据更新到 SurfaceTexture 中
//        mCameraTexture.updateTexImage();
//        // 获取 Texture 中的 Matrix
//        mCameraTexture.getTransformMatrix(mtx);
//        // 设置给 screenFilter 中
//        screenFilter.setTransformMatrix(mtx);
//        // int   数据   byte[]
//        screenFilter.onDraw(textures[0]);
//    }
//
//    //
//    @Override
//    public void onUpdated(Preview.PreviewOutput output) {
//        // 摄像头预览到的数据
//        // 问题来了，我们如何把数据丢给 gpu 进行渲染呢？
//        // attachToGLContext 和 gpu 进行绑定
//        // 调用 attachToGLContext，其实跟 mediacodec 视频硬编码非常类似
//        // mediacodec 再硬编码视频是会提供一个虚拟的 surface，
//        // 输入源（camera、录屏）等等只要在它提供的 surface 上进行渲染，
//        // mediacodec 就能对数据进行编码
//        // attachToGLContext 就提供的是一个共同源
//        mCameraTexture = output.getSurfaceTexture();
//    }
//
//    @Override
//    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//        // 一帧 一帧回调时
//        cameraView.requestRender();
//    }
//}
