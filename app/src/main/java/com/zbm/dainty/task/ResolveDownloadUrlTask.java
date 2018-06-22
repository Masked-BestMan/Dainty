package com.zbm.dainty.task;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.zbm.dainty.R;
import com.zbm.dainty.util.DownloadHelper;
import com.zbm.dainty.util.MyUtil;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zbm阿铭 on 2018/3/21.
 */
@SuppressLint("StaticFieldLeak")
public class ResolveDownloadUrlTask extends AsyncTask<String, Void, Integer> {

    private Context context;
    private HttpURLConnection conn;
    private String fileName;
    private PopupWindow myPopupWindow;
    private View anchor;   //弹出窗口的依附控件
    private File file;   // 存储路径

    public ResolveDownloadUrlTask(Context context, View anchor) {
        this.context = context;
        this.anchor = anchor;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        String downloadUrl;
        File directory;
        try {
            downloadUrl = URLDecoder.decode(strings[0], "UTF-8");
            final URL url = new URL(downloadUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3 * 1000);
            conn.setRequestMethod("GET");
            conn.connect();
            if (conn.getResponseCode() == 200) {
                fileName = getFileName(conn, downloadUrl);
                directory = new File(PreferenceManager.getDefaultSharedPreferences(context).getString("downloadPath", "/storage/emulated/0/DaintyDownloads"));
                if (!directory.exists()) {
                    //防止默认下载目录被删除了
                    if (!directory.mkdirs()) {
                        Log.d("tag", "默认下载目录创建失败");
                    }
                }

                int i = -1;
                do {
                    String fileName1 = fileName;
                    String[] s = new String[2];
                    s[0] = getFileNameNoEx(fileName1);
                    s[1] = getExtensionName(fileName1);
                    ++i;
                    if (i != 0) {
                        s[0] = s[0] + "(" + i + ")";
                    }
                    fileName1 = s[0] + "." + s[1];
                    file = new File(directory, fileName1);
                } while (file.exists());
            }
            return 200;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (result < 0) {
            Toast.makeText(context, "解析出错", Toast.LENGTH_SHORT).show();
        } else {
            initPopupWindow();
            WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
            lp.alpha = 0.6f;
            ((Activity) context).getWindow().setAttributes(lp);
            myPopupWindow.showAtLocation(anchor, Gravity.BOTTOM, 0, MyUtil.getNavigationBarHeight((Activity) context));
        }
    }

    @SuppressLint("InflateParams")
    private void initPopupWindow() {
        View popupLayout = LayoutInflater.from(context).inflate(R.layout.popup_download, null);
        final TextView filename = popupLayout.findViewById(R.id.filename);
        filename.setText(fileName);
        TextView file_size = popupLayout.findViewById(R.id.file_size);
        file_size.setText(Formatter.formatFileSize(context, conn.getContentLength()));

        Button download_start = popupLayout.findViewById(R.id.download_start);
        download_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPopupWindow.dismiss();
                for (DownloaderTask task : DownloadHelper.downloadList)
                    if (task.getFileName().equals(fileName)) {
                        Toast.makeText(context, "已存在下载任务", Toast.LENGTH_SHORT).show();
                        return;
                    }
                DownloaderTask task = new DownloaderTask(context, conn, fileName,file.getAbsolutePath());
                task.executeOnExecutor(THREAD_POOL_EXECUTOR, file);
                DownloadHelper.downloadList.add(0, task);
            }
        });
        myPopupWindow = new PopupWindow(popupLayout, WindowManager.LayoutParams.MATCH_PARENT, MyUtil.dip2px(context, 168));
        myPopupWindow.setFocusable(true);
        myPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        myPopupWindow.setAnimationStyle(R.style.download_popWindow_animation);
        myPopupWindow.setOutsideTouchable(true);
        myPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
                lp.alpha = 1f;
                ((Activity) context).getWindow().setAttributes(lp);
            }
        });
    }

    private String getFileName(HttpURLConnection conn, String downloadUrl) {
        String filename = null;
        try {
            filename = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);  //从下载路径的字符串中获取文件名称
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        if (filename == null || "".equals(filename.trim()) || !filename.contains(".")) {//如果获取不到文件名称
            for (int i = 0; ; i++) { //无限循环遍历
                String mine = conn.getHeaderField(i);   //从返回的流中获取特定索引的头字段值
                if (mine == null) break;    //如果遍历到了返回头末尾这退出循环
                if ("content-disposition".equals(conn.getHeaderFieldKey(i).toLowerCase())) {  //获取content-disposition返回头字段，里面可能会包含文件名
                    Matcher m = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase()); //使用正则表达式查询文件名
                    if (m.find()) {
                        filename = m.group(1); //如果有符合正则表达规则的字符串
                        if (filename.matches("(\").*?(\")")) {
                            return filename.substring(1, filename.length() - 1);
                        }
                    }
                }
            }
            filename = UUID.randomUUID() + ".tmp";//由网卡上的标识数字(每个网卡都有唯一的标识号)以及 CPU 时钟的唯一数字生成的的一个 16 字节的二进制作为文件名
        }

        return filename;
    }

    /*
     * Java文件操作 获取文件扩展名
     *
     */
    private static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /*
     * Java文件操作 获取不带扩展名的文件名
     */
    private static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }
}
