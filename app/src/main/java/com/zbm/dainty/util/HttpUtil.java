package com.zbm.dainty.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zbm阿铭 on 2017/5/4.
 */

public class HttpUtil {
    private static Bitmap d;
    public static void sendHttpRequest(final String address,final HttpCallbackListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection=null;
                try{

                    URL url=new URL(encodeURL(address));
                    connection=(HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in=connection.getInputStream();
                    BufferedReader reader=new BufferedReader(new InputStreamReader(in));
                    StringBuilder response=new StringBuilder();
                    String line;
                    while ((line=reader.readLine())!=null){
                        response.append(line);
                    }
                    if(listener!=null){
                        listener.onFinish(response.toString());
                    }
                }catch (Exception e){
                    if(listener!=null){
                        listener.onError(e);
                    }
                }finally{
                    if(connection!=null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public static void checkLogin(final String account,final String password,final HttpCallbackListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path="http://36078d58.nat123.cc/AndroidRegisterAndLogin/UserServlet";
                Map<String,String> params=new HashMap<>();
                params.put("account",account);
                params.put("password",password);
                try{
                    StringBuilder url=new StringBuilder(path);
                    url.append("?");
                    for(Map.Entry<String,String> entry:params.entrySet()){
                        url.append(entry.getKey()).append("=");
                        url.append(URLEncoder.encode(entry.getValue(),"UTF-8"));
                        url.append("&");
                    }
                    url.deleteCharAt(url.length()-1);
                    HttpURLConnection conn = (HttpURLConnection) new URL(url.toString()).openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setRequestMethod("GET");
                    if(conn.getResponseCode()==200) {
                        Bitmap drawable = BitmapFactory.decodeStream(conn.getInputStream());
                        if (listener != null){
                            if (drawable != null) {
                                listener.onFinish("1");
                                d = drawable;
                            } else {
                                listener.onFinish("0");
                            }
                        }
                    }else {
                        if (listener!=null){
                            listener.onFinish("-1");
                        }
                    }
                } catch (IOException e){
                    listener.onError(e);
                }
            }
        }).start();
    }
    public static Bitmap getD(){
        return d;
    }
    private static String encodeURL(String url){
        StringBuilder codeInfoUrl = new StringBuilder();
        for(int i = 0;i < url.length();i++){
            if(url.charAt(i) >= 19968)
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
        return codeInfoUrl.toString().replaceAll(" ","%20");
    }

    /*
    注意：接口方法在子线程调用，不能更新UI
     */
    public interface HttpCallbackListener{
        void onFinish(String response);
        void onError(Exception e);
    }
}
