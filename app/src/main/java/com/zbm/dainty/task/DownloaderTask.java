package com.zbm.dainty.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.zbm.dainty.ui.DownloadRecordActivity;
import com.zbm.dainty.widget.ClickableToast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created by zbm阿铭 on 2018/3/21.
 */
@SuppressLint("StaticFieldLeak")
public class DownloaderTask extends AsyncTask<File, Integer, String>{
    private Context context;
    private HttpURLConnection conn;
    private SharedPreferences preferences;
    private String fileName;

    public DownloaderTask(Context context,HttpURLConnection conn,String fileName) {
        this.context=context;
        this.conn=conn;
        this.fileName=fileName;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected String doInBackground(File... params) {
        // TODO Auto-generated method stub
        Log.d("tag", "params：" + params[0]);
        int current_write=0;
        try {
            InputStream input = conn.getInputStream();
            FileOutputStream fos = new FileOutputStream(params[0]);
            byte[] b = new byte[2048];
            int j;
            while ((j = input.read(b)) != -1) {
                if (preferences.getBoolean("isCancelDownload", false))
                    break;
                fos.write(b, 0, j);
                current_write+=j;
                publishProgress(current_write);
            }
            fos.flush();
            fos.close();
            input.close();
            if (current_write!=conn.getContentLength())return "cancel";
            else return "success";
        } catch (IOException e) {
            Log.d("ggg",e.toString());
            e.printStackTrace();
            return "fail";
        }
    }
    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();
        preferences.edit().putBoolean("isDownloading",true)
                .putString("filename",fileName)
                .putInt("total_size",conn.getContentLength())
                .apply();
        Toast.makeText(context, "开始下载", Toast.LENGTH_SHORT).show();
    }
    /*
       每隔一秒刷新一次
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Intent intent = new Intent();
        intent.setAction("download_progress_refresh");
        intent.putExtra("current_download_size",values[0]);
        if (values[0]==conn.getContentLength())intent.putExtra("finish_download",true);
        context.sendBroadcast(intent);
        Log.d("tDown","下载进度："+values[0]);
    }

    @Override
    protected void onPostExecute(final String result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        switch (result) {
            case "fail":
                preferences.edit().putBoolean("isCancelDownload", false).apply();
                Toast.makeText(context, "下载错误,请稍后再试！", Toast.LENGTH_SHORT).show();
                break;
            case "cancel":
                preferences.edit().putBoolean("isCancelDownload", false).apply();
                Toast.makeText(context, "已取消下载!", Toast.LENGTH_SHORT).show();
                break;
            default:
                ClickableToast.makeClickText(context, "下载完成", "查看", Toast.LENGTH_LONG, new ClickableToast.OnToastClickListener() {
                    @Override
                    public void onToastClick() {
                        preferences.edit().putBoolean("isDownloading", false).apply();
                        context.startActivity(new Intent(context, DownloadRecordActivity.class));
                    }
                }).show();
                break;
        }


        preferences.edit().putBoolean("isDownloading",false).apply();

        Log.d("tag:", "" + result);
        //解压
        //new ZipExtractorTask(context, result.getAbsolutePath(), directory.getAbsolutePath() + "/" + fileNameWithoutPostFix, false).execute();
    }
}
