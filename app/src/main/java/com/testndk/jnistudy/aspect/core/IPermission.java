package com.testndk.jnistudy.aspect.core;

public interface IPermission {

    void ganted(); // 已经授权

    void cancel(); // 取消授权

    void denied(); // 拒绝授权

}
