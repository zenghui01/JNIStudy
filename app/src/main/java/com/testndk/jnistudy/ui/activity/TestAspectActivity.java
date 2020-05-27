package com.testndk.jnistudy.ui.activity;

import android.view.View;

import com.testndk.jnistudy.R;
import com.testndk.jnistudy.annotation.TestAnnotation;
import com.testndk.jnistudy.utils.LogUtils;

public class TestAspectActivity extends BaseActivity {
    @Override
    public int initLayout() {
        return R.layout.activity_aspect;
    }

    @TestAnnotation
    public void onClickAspect(View view) {
        LogUtils.eLog("sssss");
    }
}
