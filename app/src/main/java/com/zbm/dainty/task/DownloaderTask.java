package com.zbm.dainty.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.zbm.dainty.ui.DownloadRecordActivity;
import com.zbm.dainty.util.DaintyDBHelper;
import com.zbm.dainty.util.DownloadHelper;
import com.zbm.dainty.widget.ClickableToast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zbm阿铭 on 2018/3/21.
 */

@SuppressLint("StaticFieldLeak")
public class DownloaderTask extends AsyncTask<String, Integer, String> {
    private Context context;
    private WeakReference<Context> contextReference;
    private String downloadUrl;
    private boolean isPause;   //暂停下载
    private String fileName;
    private File filePath;
    private int fileSize;   //文件总大小
    private long time = System.currentTimeMillis();   //下载任务创建时间
    private int progress;    //当前下载的进度
    private int downloadLength;      //已下载的长度

    public DownloaderTask(Context context, String fileName, File filePath, int fileSize, int downloadLength) {
        contextReference=new WeakReference<>(context);
        this.context=context.getApplicationContext();
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.downloadLength = downloadLength;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getProgress() {
        return progress;
    }

    public String getFilePath() {
        return filePath.getAbsolutePath();
    }

    public long getTime() {
        return time;
    }

    @Override
    protected String doInBackground(String... params) {
        // TODO Auto-generated method stub
        downloadUrl = params[0];
        int current_write = downloadLength;
        HttpURLConnection conn = null;
        RandomAccessFile raf = null;
        try {
            URL url = new URL(params[0]);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setRequestProperty("Range", "bytes=" + downloadLength + "-" + fileSize);
            raf = new RandomAccessFile(filePath, "rwd");
            raf.seek(downloadLength);
            if (conn.getResponseCode() == 206) {
                InputStream input = conn.getInputStream();
                byte[] buffer = new byte[1024 * 4];
                int len;
                while ((len = input.read(buffer)) != -1) {
                    if (!filePath.exists()) return "file_error";
                    if (isCancelled()) break;
                    raf.write(buffer, 0, len);
                    current_write += len;
                    publishProgress(current_write);
                }
            }
            return "success";
        } catch (IOException e) {
            e.printStackTrace();
            return "fail";
        } finally {
            if (conn != null)
                conn.disconnect();
            try {
                if (raf != null)
                    raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (isPause) {
            downloadLength = progress;
            Log.d("download", "onCancelled更新进度:  " + progress);
            DaintyDBHelper.getDaintyDBHelper(context).updateDownloadTable(downloadUrl,
                    filePath.getAbsolutePath(), fileName, fileSize, progress, time);
        } else {
            Log.d("download", "取消下载:  " + fileName);
            Toast.makeText(context, "下载取消", Toast.LENGTH_SHORT).show();
        }
        DownloadHelper.downloadList.remove(this);
    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();
        DaintyDBHelper.getDaintyDBHelper(context).deleteTableItem(DaintyDBHelper.DTB_NAME,
                "where downloadUrl='" + downloadUrl + "'");
        ClickableToast.makeClickText(context, "开始下载", "查看", Toast.LENGTH_SHORT,
                new ClickableToast.OnToastClickListener() {
            @Override
            public void onToastClick() {
                if (contextReference.get()!=null) {
                    Context c=contextReference.get();
                    c.startActivity(new Intent(c, DownloadRecordActivity.class));
                }
            }
        }).show();
    }

    /*
       每隔一秒刷新一次
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progress = values[0];
    }

    @Override
    protected void onPostExecute(final String result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        switch (result) {
            case "success":
                DaintyDBHelper.getDaintyDBHelper(context).deleteTableItem(DaintyDBHelper.DTB_NAME,
                        "where downloadUrl='" + downloadUrl + "'");
                break;
            case "file_error":
                DaintyDBHelper.getDaintyDBHelper(context).updateDownloadTable(downloadUrl, filePath.getAbsolutePath(), fileName, fileSize, progress, time);
                Toast.makeText(context, "文件被篡改,请重新下载！", Toast.LENGTH_SHORT).show();
                break;
            case "fail":
                if (progress==0)progress=downloadLength;
                Log.d("download","下载失败更新进度："+progress+"  downloadLength:"+downloadLength);
                DaintyDBHelper.getDaintyDBHelper(context).updateDownloadTable(downloadUrl, filePath.getAbsolutePath(), fileName, fileSize, progress, time);
                Toast.makeText(context, "下载错误,请重试！", Toast.LENGTH_SHORT).show();
                break;

        }
        Intent intent = new Intent();
        intent.setAction("download_progress_refresh");
        intent.putExtra("finish_download", true);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        DownloadHelper.downloadList.remove(this);

    }


}
