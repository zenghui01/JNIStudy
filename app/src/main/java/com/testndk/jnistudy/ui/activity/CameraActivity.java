package com.testndk.jnistudy.ui.activity;

import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.testndk.jnistudy.R;
import com.testndk.jnistudy.utils.ScreenUtil;

import java.io.IOException;

public class CameraActivity extends BaseActivity {
    SurfaceView svView;

    @Override
    public int initLayout() {
        return R.layout.activity_camera;
    }

    @Override
    public void initView() {
        super.initView();
        svView = findViewById(R.id.svView);
        SurfaceHolder holder = svView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback2() {
            @Override
            public void surfaceRedrawNeeded(SurfaceHolder holder) {

            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Camera camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                try {
                    camera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                camera.startPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }
}
