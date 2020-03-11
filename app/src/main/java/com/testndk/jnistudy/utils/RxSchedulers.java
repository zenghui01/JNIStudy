package com.testndk.jnistudy.utils;

import android.os.Looper;

import io.reactivex.FlowableTransformer;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RxSchedulers {

    private static final FlowableTransformer<?, ?> ioToMain
            = flowable -> flowable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

    @SuppressWarnings("unchecked")
    public static <T> FlowableTransformer<T, T> ioToMain() {
        return (FlowableTransformer<T, T>) ioToMain;
    }

    private static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static <T> ObservableTransformer<T, T> transformer() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
