package com.testndk.jnistudy.test;

import com.chad.library.adapter.base.entity.MultiItemEntity;

public abstract class BasePreBrowserBean implements MultiItemEntity {
    public static final int TYPE_IMG = 0;
    public static final int TYPE_IMG_GIF = 1;
    public static final int TYPE_IMG_VIDEO = 2;

    abstract String getBrowserUrl();

    abstract String getBrowserThumbUrl();

}
