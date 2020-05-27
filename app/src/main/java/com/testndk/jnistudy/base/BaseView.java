package com.testndk.jnistudy.base;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

public abstract class BaseView<P extends BasePresenter, CONTRACT> extends Activity {
    private P p;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        p = getPresenter();
        p.bindView(this);
    }

    public abstract CONTRACT getContract();

    public abstract P getPresenter();


    @Override
    protected void onDestroy() {
        super.onDestroy();
        p.unBindView();
    }
}
