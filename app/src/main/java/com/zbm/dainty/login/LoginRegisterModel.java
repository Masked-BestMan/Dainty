package com.zbm.dainty.login;

import com.zbm.dainty.util.HttpUtil;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;

public class LoginRegisterModel implements LoginRegisterContract.Model {

    private OkHttpClient okHttpClient;

    LoginRegisterModel(){
        okHttpClient=HttpUtil.getHttpClient();
    }

    @Override
    public Observable<String> login(final String account, final String oldPassword) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                String path = "http://36078d58.nat123.cc/AndroidRegisterAndLogin_war/login";
                Map<String, String> params = new HashMap<>();
                params.put("username", account);
                params.put("password", oldPassword);

                ResponseBody responseBody=executeHttp(path,params);
                if (responseBody != null) {
                    String result = responseBody.string().replaceAll("(\\\r\\\n|\\\r|\\\n|\\\n\\\r)", "");
                    emitter.onNext(result);
                    responseBody.close();
                } else {
                    emitter.onError(new Throwable("响应体为空"));
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<String> register(final String account, final String oldPassword, final String newPassword) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                String path = "http://36078d58.nat123.cc/AndroidRegisterAndLogin_war/register";
                Map<String, String> params = new HashMap<>();
                params.put("username", account);
                params.put("password", oldPassword);
                params.put("newPassword",newPassword);

                ResponseBody responseBody=executeHttp(path,params);
                if (responseBody != null) {
                    String result = responseBody.string().replaceAll("(\\\r\\\n|\\\r|\\\n|\\\n\\\r)", "");
                    emitter.onNext(result);
                    responseBody.close();
                } else {
                    emitter.onError(new Throwable("响应体为空"));
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<String> modifyPassword(final String account, final String oldPassword, final String newPassword) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                String path = "http://36078d58.nat123.cc/AndroidRegisterAndLogin_war/modify";
                Map<String, String> params = new HashMap<>();
                params.put("username", account);
                params.put("password", oldPassword);
                params.put("newPassword",newPassword);

                ResponseBody responseBody=executeHttp(path,params);
                if (responseBody != null) {
                    String result = responseBody.string().replaceAll("(\\\r\\\n|\\\r|\\\n|\\\n\\\r)", "");
                    emitter.onNext(result);
                    responseBody.close();
                } else {
                    emitter.onError(new Throwable("响应体为空"));
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    private ResponseBody executeHttp(String path,Map<String,String> params) throws Exception {
        StringBuilder url = new StringBuilder(path);
        url.append("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            url.append(entry.getKey()).append("=");
            url.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            url.append("&");
        }
        url.deleteCharAt(url.length() - 1);
        Request request = new Request.Builder()
                .url(url.toString())
                .build();
        Response response = okHttpClient.newCall(request).execute();
        return response.body();
    }
}
