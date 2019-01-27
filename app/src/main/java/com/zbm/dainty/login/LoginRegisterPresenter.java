package com.zbm.dainty.login;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class LoginRegisterPresenter implements LoginRegisterContract.Presenter {

    private LoginRegisterModel model;
    private CompositeDisposable disposable;
    private LoginRegisterContract.View view;

    LoginRegisterPresenter(LoginRegisterContract.View view) {
        model = new LoginRegisterModel();
        disposable = new CompositeDisposable();
        this.view = view;
    }

    @Override
    public void unsubscribe() {
        disposable.clear();
    }

    @Override
    public void login(String account, String oldPassword) {
        disposable.add(model.login(account, oldPassword).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) {
                view.showToast(s);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                view.showToast(throwable.getMessage());
            }
        }));
    }

    @Override
    public void register(String account, String oldPassword, String newPassword) {
        disposable.add(model.register(account, oldPassword, newPassword)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        view.showToast(s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        view.showToast(throwable.getMessage());
                    }
                }));
    }

    @Override
    public void modifyPassword(String account, String oldPassword, String newPassword) {
        disposable.add(model.modifyPassword(account, oldPassword, newPassword)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        view.showToast(s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        view.showToast(throwable.getMessage());
                    }
                }));
    }
}
