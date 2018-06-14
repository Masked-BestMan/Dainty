package com.zbm.dainty.util;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.json.JSONObject;

/**
 * Created by Zbm阿铭 on 2017/5/1.
 */

public class WeatherService extends Service {
    private String city="";
    private LocationClient mLocationClient = null;

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        mLocationClient = new LocationClient(getApplicationContext());
        MyLocationListener myListener = new MyLocationListener();
        initLocation();
        mLocationClient.registerLocationListener(myListener);
        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
        city=sharedPreferences.getString("cityName","");

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
    private void initLocation(){
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
    private class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if(bdLocation.getLocType()==63)
                Toast.makeText(WeatherService.this,"请检查网络",Toast.LENGTH_SHORT).show();
            else{
                city=bdLocation.getDistrict();   //具体到区县的定位
                SharedPreferences.Editor pref= PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                pref.putString("cityName",city);
                pref.apply();

                if(city!=null){
                    HttpUtil.sendHttpRequest("http://wthrcdn.etouch.cn/weather_mini?city=" + city, new HttpUtil.HttpCallbackListener() {
                        @Override
                        public void onFinish(String response) {
                            handleWeatherResponse(WeatherService.this,response);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                }

            }
            WeatherService.this.stopSelf();
        }
    }
    private void handleWeatherResponse(Context context, String response){
        try {
            Log.d("ttt",response);
            JSONObject jsonObject=new JSONObject(response);
            JSONObject weatherInfo=jsonObject.getJSONObject("data");
            String temperature=weatherInfo.getString("wendu");
            String climate=weatherInfo.getString("ganmao");
            SharedPreferences.Editor pref= PreferenceManager.getDefaultSharedPreferences(context).edit();
            pref.putString("wendu",temperature+"°");
            pref.putString("ganmao",climate);
            pref.apply();
            context.getApplicationContext().sendBroadcast(new Intent("weather_refresh"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
