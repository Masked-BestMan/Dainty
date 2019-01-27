package com.zbm.dainty.login;

import com.zbm.dainty.base.BaseModel;
import com.zbm.dainty.base.BasePresenter;
import com.zbm.dainty.base.BaseView;

import io.reactivex.Observable;

public interface LoginRegisterContract {

    interface View extends BaseView{

    }

    interface Presenter extends BasePresenter{
        void login(String account, String oldPassword);
        void register(String account, String oldPassword, String newPassword);
        void modifyPassword(String account, String oldPassword, String newPassword);
    }

    interface Model extends BaseModel{
        Observable<String> login(String account, String oldPassword);
        Observable<String> register(String account, String oldPassword, String newPassword);
        Observable<String> modifyPassword(String account, String oldPassword, String newPassword);
    }
}