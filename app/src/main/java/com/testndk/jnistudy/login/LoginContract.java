package com.testndk.jnistudy.login;

import com.testndk.jnistudy.bean.BaseEntity;

public interface LoginContract {
    interface Model{
        void exLogin(String name,String pwd);
    }

    interface View<T extends BaseEntity>{
        //请求结果往往是jeanbean
        void handleResult(T t);
    }

    interface Presenter<T extends BaseEntity>{
        void requestLogin(String name,String pwd);
        void responseResult(T t);
    }
}
