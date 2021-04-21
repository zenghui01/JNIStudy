package com.testndk.jnistudy;

import android.app.Application;

import com.testndk.jnistudy.utils.ScreenUtil;

public class MyApplication extends Application {
    public static MyApplication INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        ScreenUtil.init(this);
        registerActivityLifecycleCallbacks(MyActivityLifecycleCallbacks.getInstance());
//        CrashCollectHandler.Companion.getInstance().init(this);
    }
}
