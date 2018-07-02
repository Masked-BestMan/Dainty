package com.zbm.dainty.task;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.zbm.dainty.ui.DownloadRecordActivity;
import com.zbm.dainty.widget.ClickableToast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class ImageTask extends AsyncTask<String, Void, String> {

    private WeakReference<Context> contextReference;
    private String dir;

    public ImageTask(Context context) {
        contextReference=new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (contextReference.get()!=null)
            dir = PreferenceManager.getDefaultSharedPreferences(contextReference.get()).getString("downloadPath", "/storage/emulated/0/DaintyDownloads");
    }

    @Override
    protected String doInBackground(String... strings) {
        InputStream is = null;
        FileOutputStream out = null;
        try {
            Calendar now = new GregorianCalendar();
            SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String fileName = simpleDate.format(now.getTime());
            URL url = new URL(strings[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.connect();
            is = conn.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            File file = new File(dir + File.separator + fileName + ".jpg");
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return "fail";
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return "success";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (contextReference.get()!=null) {
            final Context context=contextReference.get();
            if (s.equals("success"))
                ClickableToast.makeClickText(context, "保存成功", "查看", Toast.LENGTH_SHORT, new ClickableToast.OnToastClickListener() {
                    @Override
                    public void onToastClick() {
                        context.startActivity(new Intent(context, DownloadRecordActivity.class));
                    }
                }).show();
            else
                Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }
}
