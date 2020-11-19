package com.testndk.jnistudy.ui.activity;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.testndk.jnistudy.R;
import com.testndk.jnistudy.ui.rtmp.Pusher;
import com.testndk.jnistudy.utils.LogUtils;

public class RTMPActivity extends BaseActivity{
    Pusher pusher;

    FrameLayout flParent;

    @Override
    public int initLayout() {
        return R.layout.activity_rtmp;
    }

    @Override
    public void initView() {
        super.initView();
        flParent = findViewById(R.id.flParent);
        SurfaceView surfaceView=new SurfaceView(this);
        flParent.addView(surfaceView);
        pusher = new Pusher(this);
        pusher.setPreviewDisplay(surfaceView.getHolder());
    }

    public void switchCamera(View view) {
        pusher.switchCamera();
    }

    public void startLive(View view) {
        pusher.startLive("rtmp://47.96.225.33/myapp/");
    }

    public void stopLive(View view) {
        pusher.stopLive();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pusher.release();
    }

}
