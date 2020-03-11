package com.testndk.jnistudy.ui.activity;

import android.view.View;

import com.testndk.jnistudy.R;
import com.testndk.jnistudy.ui.bsdiff.BsdiffUtilsJava;

public class BsdiffActivity extends BaseActivity {
    @Override
    public int initLayout() {
        return R.layout.activity_bsdiff;
    }

    public void onClickMerge(View view) {
        BsdiffUtilsJava.mergeApk(this);
    }
}
