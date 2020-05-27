package com.testndk.jnistudy.base;

import java.lang.ref.WeakReference;

public abstract class BasePresenter<V extends BaseView, M extends BaseModel, CONTRACT> {
    private M m;
    private WeakReference<V> vWeakReference;

    public BasePresenter() {
        m = getModel();
    }

    public void bindView(V v) {
        vWeakReference = new WeakReference<>(v);
    }

    public void unBindView() {
        if (vWeakReference != null) {
            vWeakReference.clear();
            vWeakReference = null;
            System.gc();
        }
    }

    public V getView() {
        if (vWeakReference != null) {
            return vWeakReference.get();
        }
        return null;
    }

    public abstract CONTRACT getContract();

    public abstract M getModel();
}
