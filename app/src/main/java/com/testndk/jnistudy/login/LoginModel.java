package com.testndk.jnistudy.login;

import com.testndk.jnistudy.base.BaseModel;

public class LoginModel extends BaseModel<LoginPresenter,LoginContract.Model> {
    public LoginModel(LoginPresenter loginPresenter) {
        super(loginPresenter);
    }

    @Override
    public LoginContract.Model getContract() {
        return null;
    }
}
