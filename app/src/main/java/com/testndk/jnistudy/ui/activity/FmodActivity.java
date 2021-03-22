package com.testndk.jnistudy.ui.activity;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.testndk.jnistudy.R;

import org.fmod.FMOD;

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
        stopPlayVoice();
        FMOD.close();
        handler = null;
    }

    public void onFomdCallback(String s) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                tvNotice.append(s + "\n");
            }
        });
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

    public void onCLickStop(View view) {
        stopPlayVoice();
    }

    public native void playVoice(int model, String path);

    public native void stopPlayVoice();
}
