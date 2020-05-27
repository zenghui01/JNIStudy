package com.testndk.jnistudy.ui.activity;

import android.view.View;

import com.testndk.jnistudy.R;
import com.testndk.jnistudy.ui.fmod.FmodUtils;

public class FmodActivity extends BaseActivity {
    @Override
    public int initLayout() {
        return R.layout.activity_fmod;
    }

    @Override
    public void initView() {
        super.initView();
        FmodUtils.init(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FmodUtils.close();
    }

    public void onClickNormal(View view) {
    }

    public void onClickLuoli(View view) {
    }

    public void onClickDashu(View view) {
    }

    public void onClickJingsong(View view) {
    }

    public void onClickGaoguai(View view) {
    }

    public void onClickKongling(View view) {
    }
}
