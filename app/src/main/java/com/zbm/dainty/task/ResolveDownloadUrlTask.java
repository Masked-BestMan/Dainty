package com.zbm.dainty.task;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.zbm.dainty.R;
import com.zbm.dainty.bean.FileDownloadBean;
import com.zbm.dainty.ui.DownloadRecordActivity;
import com.zbm.dainty.util.DaintyDBHelper;
import com.zbm.dainty.util.DownloadHelper;
import com.zbm.dainty.util.FileUtil;
import com.zbm.dainty.util.MyUtil;
import com.zbm.dainty.widget.ClickableToast;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zbm阿铭 on 2018/3/21.
 */
@SuppressLint("StaticFieldLeak")
public class ResolveDownloadUrlTask extends AsyncTask<String, Void, Integer> {

    private Context context;
    private String fileName;
    private int fileSize;
    private PopupWindow myPopupWindow;
    private View anchor;   //弹出窗口的依附控件
    private File file;   // 存储路径
    private String downloadUrl;

    public ResolveDownloadUrlTask(Context context, View anchor) {
        this.context = context;
        this.anchor = anchor;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        HttpURLConnection conn = null;
        File directory;
        try {
            downloadUrl = URLDecoder.decode(strings[0], "UTF-8");
            final URL url = new URL(downloadUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8 * 1000);
            conn.setRequestMethod("GET");
            conn.connect();
            if (conn.getResponseCode() == 200) {
                fileSize = conn.getContentLength();
                fileName = getFileName(conn, downloadUrl);
                directory = new File(PreferenceManager.getDefaultSharedPreferences(context).getString("downloadPath", "/storage/emulated/0/DaintyDownloads"));
                if (!directory.exists()) {
                    //防止默认下载目录被删除了
                    if (!directory.mkdirs()) {
                        Log.d("tag", "默认下载目录创建失败");
                    }
                }

                int i = -1;
                String fileName1;
                do {
                    fileName1 = fileName;
                    String[] s = new String[2];
                    s[0] = FileUtil.getFileNameNoEx(fileName1);
                    s[1] = FileUtil.getExtensionName(fileName1);
                    ++i;
                    if (i != 0) {
                        s[0] = s[0] + "(" + i + ")";
                    }
                    fileName1 = s[0] + "." + s[1];
                    file = new File(directory, fileName1);
                } while (file.exists());
                fileName = fileName1;
            }
            return 200;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            if (conn != null)
                conn.disconnect();
        }
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
        Button fileNameEdit = popupLayout.findViewById(R.id.filename_edit);
        fileNameEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditableDialog();
            }
        });
        TextView file_size = popupLayout.findViewById(R.id.file_size);
        file_size.setText(Formatter.formatFileSize(context, fileSize));

        Button download_start = popupLayout.findViewById(R.id.download_start);
        download_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPopupWindow.dismiss();

                //先检查是否是正在下载的文件
                for (DownloaderTask downloaderTask : DownloadHelper.downloadList)
                    if (downloaderTask.getDownloadUrl().equals(downloadUrl)) {
                        ClickableToast.makeClickText(context, "已存在下载任务", "查看", Toast.LENGTH_SHORT, new ClickableToast.OnToastClickListener() {
                            @Override
                            public void onToastClick() {
                                context.startActivity(new Intent(context, DownloadRecordActivity.class));
                            }
                        }).show();
                        return;
                    }
                //再检查是否是暂停下载的文件
                DaintyDBHelper.getDaintyDBHelper(context).searchDownloadTable("select * from " + DaintyDBHelper.DTB_NAME + " where downloadUrl='" + downloadUrl + "'", new DaintyDBHelper.OnSearchDownloadTableListener() {
                    @Override
                    public void onResult(ArrayList<FileDownloadBean> mDownloadData) {
                        if (mDownloadData.size() > 0) {
                            ClickableToast.makeClickText(context, "已存在下载任务", "查看", Toast.LENGTH_SHORT, new ClickableToast.OnToastClickListener() {
                                @Override
                                public void onToastClick() {
                                    context.startActivity(new Intent(context, DownloadRecordActivity.class));
                                }
                            }).show();
                            return;
                        }
                        //允许正在下载的文件数为3
                        if (DownloadHelper.downloadList.size() == DownloadHelper.downloadLimitCount) {
                            DaintyDBHelper.getDaintyDBHelper(context).updateDownloadTable(downloadUrl,file.getAbsolutePath(),fileName,fileSize,0,System.currentTimeMillis());
                            ClickableToast.makeClickText(context, "以存入等候队列", "查看", Toast.LENGTH_SHORT, new ClickableToast.OnToastClickListener() {
                                @Override
                                public void onToastClick() {
                                    context.startActivity(new Intent(context, DownloadRecordActivity.class));
                                }
                            }).show();
                            return;
                        }
                        DownloaderTask task = new DownloaderTask(context, fileName, file, fileSize, 0);
                        task.executeOnExecutor(THREAD_POOL_EXECUTOR, downloadUrl);
                        DownloadHelper.downloadList.add(task);
                    }

                });


            }
        });
        myPopupWindow = new PopupWindow(popupLayout, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
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
            filename = "未命名.tmp";
        }

        return filename;
    }


    /**
     * 显示修改文件名对话框
     */
    private void showEditableDialog() {
        myPopupWindow.dismiss();
        final String suffix = FileUtil.getExtensionName(fileName);
        final EditText et = new EditText(context);
        et.setText(FileUtil.getFileNameNoEx(fileName));
        new AlertDialog.Builder(context).setTitle("请输入新的文件名")
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        fileName = et.getText().toString() + "." + suffix;
                        initPopupWindow();
                        WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
                        lp.alpha = 0.6f;
                        ((Activity) context).getWindow().setAttributes(lp);

                        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (inputMethodManager != null && MyUtil.isSoftInputMethodShowing((Activity) context))
                            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                        myPopupWindow.showAtLocation(anchor, Gravity.BOTTOM, 0, MyUtil.getNavigationBarHeight((Activity) context));
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null && MyUtil.isSoftInputMethodShowing((Activity) context))
                    inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                myPopupWindow.showAtLocation(anchor, Gravity.BOTTOM, 0, MyUtil.getNavigationBarHeight((Activity) context));
            }
        }).show();

    }
}
