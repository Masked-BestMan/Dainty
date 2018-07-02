package com.zbm.dainty.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;

import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;

import java.io.File;

public class ClearCacheTask extends AsyncTask<String,Void,Boolean> {
    @SuppressLint("StaticFieldLeak")
    private Context context;
    public ClearCacheTask(Context context){
        this.context=context.getApplicationContext();
    }
    @Override
    protected Boolean doInBackground(String... strings) {
        String a=strings[0].substring(1,strings[0].length()-1);
        String[] ss=a.split(", ");
        for(String s:ss){
            switch (s){
                case "1":
                    //清除会话和持久态Cookies（保持网页登录状态，偏好设置）
                    CookieManager cm=CookieManager.getInstance();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        cm.removeAllCookies(null);
                        cm.flush();
                    } else {
                        cm.removeAllCookie();
                        CookieSyncManager.getInstance().sync();
                    }
                    break;
                case "2":
                    deleteFile(new File(context.getDir("webview",0).getPath()+"/Cache"));
                    break;
                case "3":
                    deleteFile(new File(context.getDir("webview",0).getPath()+"/Local Storage"));
                    break;
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        Toast.makeText(context,"清理完成",Toast.LENGTH_SHORT).show();
    }

    private void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                deleteFile(f);
            }
            //file.delete(); //如要保留文件夹，只删除文件，请注释这行
        } else if (file.exists()) {
            file.delete();
        }
    }
}
