package com.zbm.dainty.main;


import com.zbm.dainty.bean.WeatherInfoBean;
import com.zbm.dainty.util.HttpUtil;

import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainModel implements MainContract.Model {

    private OkHttpClient okHttpClient;

    MainModel(){
        okHttpClient=HttpUtil.getHttpClient();
    }

    @Override
    public Observable<WeatherInfoBean> getWeatherInfo(final String city) {
        return Observable.create(new ObservableOnSubscribe<WeatherInfoBean>() {
            @Override
            public void subscribe(ObservableEmitter<WeatherInfoBean> emitter) throws Exception {
                String path="http://wthrcdn.etouch.cn/weather_mini?city=" + city;
                Request request = new Request.Builder()
                        .url(path)
                        .build();
                Response response = okHttpClient.newCall(request).execute();
                ResponseBody responseBody=response.body();
                if (responseBody!=null){
                    JSONObject jsonObject=new JSONObject(responseBody.string());
                    JSONObject weatherInfo=jsonObject.getJSONObject("data");
                    String temperature=weatherInfo.getString("wendu")+"°";
                    String climate=weatherInfo.getString("ganmao");
                    emitter.onNext(new WeatherInfoBean(city,temperature,climate));
                }else {
                    emitter.onError(new Throwable("响应体为空"));
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
