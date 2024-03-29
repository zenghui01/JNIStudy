package com.testndk.jnistudy.ui.opengl.one;

import android.opengl.GLSurfaceView;

import com.testndk.jnistudy.R;
import com.testndk.jnistudy.ui.activity.BaseActivity;

public class OpenGLActivity extends BaseActivity {

    @Override
    public int initLayout() {
        return R.layout.activity_opengl;
    }

    private GLSurfaceView glSurfaceView;

    @Override
    public void initView() {
        glSurfaceView = findViewById(R.id.glSurfaceView);
        // GLContext设置OpenGLES2.0
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(new TriangleRender());
        /*渲染方式，RENDERMODE_WHEN_DIRTY表示被动渲染，
		  只有在调用requestRender或者onResume等方法时才会进行渲染。
		  RENDERMODE_CONTINUOUSLY表示持续渲染*/
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }


}