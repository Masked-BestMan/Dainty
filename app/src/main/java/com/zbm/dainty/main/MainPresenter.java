package com.zbm.dainty.main;

import com.zbm.dainty.bean.WeatherInfoBean;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class MainPresenter implements MainContract.Presenter {

    private CompositeDisposable disposable;
    private MainModel model;
    private MainContract.View view;

    MainPresenter(MainContract.View view){
        disposable=new CompositeDisposable();
        model=new MainModel();
        this.view=view;
    }

    @Override
    public void getWeatherInfo(String city) {
        disposable.add(model.getWeatherInfo(city).subscribe(new Consumer<WeatherInfoBean>() {
            @Override
            public void accept(WeatherInfoBean weatherInfoBean) {
                view.showWeatherInfo(weatherInfoBean);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {

            }
        }));
    }

    @Override
    public void unsubscribe() {
        disposable.clear();
    }
}
