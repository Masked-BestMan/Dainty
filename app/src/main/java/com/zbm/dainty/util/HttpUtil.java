package com.zbm.dainty.util;


import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by Zbm阿铭 on 2017/5/4.
 */

public class HttpUtil {

    private OkHttpClient okHttpClient;

    private HttpUtil() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();
    }

    private static class Holder {
        private static HttpUtil httpUtil = new HttpUtil();
    }

    public static HttpUtil getInstance() {
        return Holder.httpUtil;
    }

    public static void sendHttpRequest(final String address, final HttpCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {

                    URL url = new URL(encodeURL(address));
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    if (listener != null) {
                        listener.onFinish(response.toString());
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        listener.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public Observable<String> checkLogin(final int type, final String account, final String oldPassword, final String newPassword) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                String baseUrl = "http://36078d58.nat123.cc/AndroidRegisterAndLogin_war/";
                String path;
                switch (type) {
                    case 0:
                        path = baseUrl + "login";
                        break;
                    case 1:
                        path = baseUrl + "register";
                        break;
                    default:
                        path = baseUrl + "modify";
                }
                Map<String, String> params = new HashMap<>();
                params.put("username", account);
                params.put("password", oldPassword);
                if (!TextUtils.isEmpty(newPassword)){
                    params.put("newPassword",newPassword);
                }
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
                ResponseBody responseBody = response.body();
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

    public static void checkLogin(final String account, final String password, final HttpCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = "http://36078d58.nat123.cc/AndroidRegisterAndLogin_war/login";
                Map<String, String> params = new HashMap<>();
                params.put("username", account);
                params.put("password", password);
                try {
                    StringBuilder url = new StringBuilder(path);
                    url.append("?");
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        url.append(entry.getKey()).append("=");
                        url.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                        url.append("&");
                    }
                    url.deleteCharAt(url.length() - 1);
                    HttpURLConnection conn = (HttpURLConnection) new URL(url.toString()).openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setRequestMethod("GET");
                    if (conn.getResponseCode() == 200) {
//                        Bitmap drawable = BitmapFactory.decodeStream(conn.getInputStream());
//                        if (listener != null){
//                            if (drawable != null) {
//                                listener.onFinish("1");
//                                d = drawable;
//                            } else {
//                                listener.onFinish("0");
//                            }
//                        }
                        InputStream inputStream = conn.getInputStream();
                        final int bufferSize = 1024;
                        final char[] buffer = new char[bufferSize];
                        final StringBuilder out = new StringBuilder();
                        Reader in = new InputStreamReader(inputStream, "UTF-8");
                        for (; ; ) {
                            int rsz = in.read(buffer, 0, buffer.length);
                            if (rsz < 0)
                                break;
                            out.append(buffer, 0, rsz);
                        }
                        inputStream.close();
                        String i = out.toString().replaceAll("(\\\r\\\n|\\\r|\\\n|\\\n\\\r)", "");
                        if (i.equals("登录成功")) {
                            listener.onFinish("1");
                        } else {
                            listener.onFinish("0");
                        }
                    } else {
                        if (listener != null) {
                            listener.onFinish("-1");
                        }
                    }
                } catch (IOException e) {
                    listener.onError(e);
                }
            }
        }).start();
    }

    private static String encodeURL(String url) {
        StringBuilder codeInfoUrl = new StringBuilder();
        for (int i = 0; i < url.length(); i++) {
            if (url.charAt(i) >= 19968)
                try {
                    codeInfoUrl.append(URLEncoder.encode(url.substring(i, i + 1), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            else {
                codeInfoUrl.append(url.substring(i, i + 1));
            }
        }
        return codeInfoUrl.toString().replaceAll(" ", "%20");
    }

    /*
    注意：接口方法在子线程调用，不能更新UI
     */
    public interface HttpCallbackListener {
        void onFinish(String response);

        void onError(Exception e);
    }
}
