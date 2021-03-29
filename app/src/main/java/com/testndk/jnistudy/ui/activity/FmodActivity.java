package com.testndk.jnistudy.ui.activity;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.testndk.jnistudy.R;

import org.fmod.FMOD;
public class FmodActivity extends BaseActivity {

    static {
        System.loadLibrary("fmod_lib");
    }

    public static final int MODEL_NORMAL = 0;
    public static final int MODEL_LUOLI = 1;
    public static final int MODEL_DASHU = 2;
    public static final int MODEL_JINGSONG = 3;
    public static final int MODEL_GAOGUAI = 4;
    public static final int MODEL_KONGLING = 5;
    private String file_path;

    TextView tvNotice;

    Handler handler = new Handler();

    @Override
    public int initLayout() {
        return R.layout.activity_fmod;
    }

    @Override
    public void initView() {
        super.initView();
        tvNotice = findViewById(R.id.tvNotice);
        FMOD.init(this);
        file_path = "file:///android_asset/test.m4a";
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                playVoice(MODEL_NORMAL, file_path);
            }
        }).start();
    }

    public void onClickLuoli(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                playVoice(MODEL_LUOLI, file_path);
            }
        }).start();
    }

    public void onClickDashu(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                playVoice(MODEL_DASHU, file_path);
            }
        }).start();
    }

    public void onClickJingsong(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                playVoice(MODEL_JINGSONG, file_path);
            }
        }).start();
    }

    public void onClickGaoguai(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                playVoice(MODEL_GAOGUAI, file_path);
            }
        }).start();
    }

    public void onClickKongling(View view) {
    }

    public void onCLickStop(View view) {
        stopPlayVoice();
    }

    public native void playVoice(int model, String path);

    public native void stopPlayVoice();
}
