package com.zbm.dainty.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.zbm.dainty.ui.DownloadRecordActivity;
import com.zbm.dainty.util.DownloadHelper;
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
    private String fileName;
    private String filePath;
    private long time=System.currentTimeMillis();   //下载任务创建时间
    private int progress;
    DownloaderTask(Context context, HttpURLConnection conn, String fileName, String filePath) {
        this.context=context;
        this.conn=conn;
        this.fileName=fileName;
        this.filePath=filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileSize(){
        return conn.getContentLength();
    }

    public int getProgress() {
        return progress;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getTime() {
        return time;
    }

    @Override
    protected String doInBackground(File... params) {
        // TODO Auto-generated method stub
        int current_write=0;
        try {
            InputStream input = conn.getInputStream();
            FileOutputStream fos = new FileOutputStream(params[0]);
            byte[] b = new byte[2048];
            int j;
            while ((j = input.read(b)) != -1) {
                if (isCancelled())break;
                fos.write(b, 0, j);
                current_write+=j;
                publishProgress(current_write);
            }
            fos.flush();
            fos.close();
            input.close();
            return "success";
        } catch (IOException e) {
            e.printStackTrace();
            return "fail";
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.d("Dainty","取消下载:  "+fileName);
        Toast.makeText(context,"任务取消",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();
        Toast.makeText(context, "开始下载", Toast.LENGTH_SHORT).show();
    }
    /*
       每隔一秒刷新一次
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progress=values[0];
    }

    @Override
    protected void onPostExecute(final String result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        switch (result) {
            case "fail":
                Toast.makeText(context, "下载错误,请重新下载！", Toast.LENGTH_SHORT).show();
                break;
            default:
                //DownloadHelper.downloadList.remove(this);
                Intent intent = new Intent();
                intent.setAction("download_progress_refresh");
                intent.putExtra("finish_download",true);
                context.sendBroadcast(intent);
                ClickableToast.makeClickText(context, "下载完成", "查看", Toast.LENGTH_LONG, new ClickableToast.OnToastClickListener() {
                    @Override
                    public void onToastClick() {
                        context.startActivity(new Intent(context, DownloadRecordActivity.class));
                    }
                }).show();
                break;
        }
        DownloadHelper.downloadList.remove(this);
        //解压
        //new ZipExtractorTask(context, result.getAbsolutePath(), directory.getAbsolutePath() + "/" + fileNameWithoutPostFix, false).execute();
    }
}
