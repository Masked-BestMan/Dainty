package com.zbm.dainty.main;

import com.zbm.dainty.base.BaseModel;
import com.zbm.dainty.base.BasePresenter;
import com.zbm.dainty.base.BaseView;
import com.zbm.dainty.bean.WeatherInfoBean;

import io.reactivex.Observable;

public interface MainContract {
    interface View extends BaseView{
        void showWeatherInfo(WeatherInfoBean weatherInfoBean);
    }

    interface Model extends BaseModel{
        Observable<WeatherInfoBean> getWeatherInfo(String city);
    }

    interface Presenter extends BasePresenter{
        void getWeatherInfo(String city);
    }
}
