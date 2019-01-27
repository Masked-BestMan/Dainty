package com.zbm.dainty.main;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.zbm.dainty.bean.WeatherInfoBean;

/**
 * Created by Zbm阿铭 on 2017/5/1.
 */

public class WeatherService extends Service implements MainContract.View {
    private String city = "";
    private LocationClient mLocationClient = null;
    private MainPresenter presenter;

    @Override
    public void onCreate() {
        super.onCreate();
        presenter = new MainPresenter(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLocationClient = new LocationClient(getApplicationContext());
        MyLocationListener myListener = new MyLocationListener();
        initLocation();
        mLocationClient.registerLocationListener(myListener);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        city = sharedPreferences.getString("cityName", "");

        mLocationClient.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps

        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集

        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        mLocationClient.setLocOption(option);
    }

    @Override
    public void showWeatherInfo(WeatherInfoBean weatherInfoBean) {
        Intent intent = new Intent("weather_refresh");
        intent.putExtra("content", weatherInfoBean);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation.getLocType() == 63)
                Toast.makeText(WeatherService.this, "请检查网络", Toast.LENGTH_SHORT).show();
            else {
                city = bdLocation.getDistrict();   //具体到区县的定位
                SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                pref.putString("cityName", city);
                pref.apply();

                if (city != null) {
                    presenter.getWeatherInfo(city);
                }

            }
            WeatherService.this.stopSelf();
        }
    }
}
