package com.zbm.dainty.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zbm.dainty.DaintyApplication;
import com.zbm.dainty.adapter.DownloadRecordAdapter;
import com.zbm.dainty.R;
import com.zbm.dainty.widget.SwipeBackActivity;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by zbm阿铭 on 2018/3/18.
 */

public class DownloadRecordActivity extends SwipeBackActivity {
    @BindView(R.id.download_record_bar_theme)
    View downloadRecordBarTheme;
    @BindView(R.id.download_record_back)
    Button downloadRecordBack;
    @BindView(R.id.downloading_container)
    View downloadContainer;
    @BindView(R.id.downloading_file_icon)
    ImageView downloadingFileIcon;
    @BindView(R.id.downloading_filename)
    TextView downloadingFilename;
    @BindView(R.id.stop_download)
    ImageView stopDownload;
    @BindView(R.id.download_speed)
    TextView downloadSpeed;
    @BindView(R.id.download_progress)
    ProgressBar downloadProgress;
    @BindView(R.id.download_record_list)
    ListView downloadRecordList;
    @BindView(R.id.empty_download_record)
    ImageView emptyRecord;
    @BindView(R.id.download_record_select_more_bar)
    View selectMoreBar;
    @BindView(R.id.download_record_confirm_delete)
    Button confirmDelete;
    @BindView(R.id.download_record_cancel_delete)
    Button cancelDelete;

    private boolean isDownloading=false;
    private DownloadRecordAdapter adapter;
    private List<Map<String,Object>> data=new ArrayList<>();
    private List<Integer> selectedItemList = new ArrayList<>();
    private int selectedPosition;
    private PopupWindow deleteWindow;
    private Timer timer;
    private long lastTotalRxBytes = 0;
    private File[] files;    //下载目录内的文件
    private UpdateDownloadSpeedHandler mHandler = new UpdateDownloadSpeedHandler(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter mFilter=new IntentFilter();
        mFilter.addAction("download_progress_refresh");
        registerReceiver(downloadStatus,mFilter);
        lastTotalRxBytes=getTotalRxBytes();
        setContentView(R.layout.activity_download_record);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        ButterKnife.bind(this);
        initData();
        initView();

    }
    private void initView(){
        adapter=new DownloadRecordAdapter(this,data);
        downloadRecordList.setAdapter(adapter);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        downloadRecordBarTheme.setBackgroundColor(Color.parseColor(preferences.getString("theme_color", "#474747")));
        downloadingFileIcon.setImageResource(R.drawable.s360_icon);
        isDownloading=preferences.getBoolean("isDownloading",false);
        if (isDownloading){
            timer=new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    showNetSpeed();
                    Log.d("uuu","每隔一秒");
                }
            },0,1000);
            downloadContainer.setVisibility(View.VISIBLE);
            downloadingFilename.setText(preferences.getString("filename",""));
            downloadProgress.setMax(preferences.getInt("total_size", 100));
            stopDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder normalDialog =
                            new AlertDialog.Builder(DownloadRecordActivity.this);
                    normalDialog.setIcon(android.R.drawable.ic_menu_info_details)
                            .setTitle("关闭下载提示")
                            .setMessage("有下载任务正在进行，关闭下载栏将删除临时文件，仍要关闭？")
                            .setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            preferences.edit().putBoolean("isCancelDownload", true).apply();
                                            preferences.edit().putBoolean("isDownloading",false).apply();
                                            downloadContainer.setVisibility(View.GONE);
                                            timer.cancel();
                                            timer=null;
                                            if(!((DaintyApplication)DownloadRecordActivity.this.getApplication()).getTemporaryDownloadFile().delete()){
                                                Log.d("sas","删除失败");
                                            }
                                        }
                                    })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).show();
                }
            });
        }
        downloadRecordBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        downloadRecordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter.isCanSelectMore()){
                    CheckBox itemCheckBox = view.findViewById(R.id.download_record_delete_checkbox);
                    if (itemCheckBox.isChecked()) {
                        itemCheckBox.setChecked(false);

                    } else {
                        itemCheckBox.setChecked(true);
                    }
                }else {
                    startActivity(getFileIntent((File) data.get(position).get("file_path")));
                }

            }
        });
        downloadRecordList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition= position;
                deleteWindow.showAsDropDown(view, 50, 50, Gravity.BOTTOM);
                return true;
            }
        });
        adapter.setOnCheckChangedListener(new DownloadRecordAdapter.OnCheckChangedListener() {
            @Override
            public void onCheckChanged(int position, boolean checked) {
                if (checked){
                    selectedItemList.add(position);
                }else {
                    selectedItemList.remove((Integer) position);
                }
                Log.d("rer","selected:"+selectedItemList.size());
            }
        });
        emptyRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder normalDialog =
                        new AlertDialog.Builder(DownloadRecordActivity.this);
                normalDialog.setIcon(android.R.drawable.ic_menu_info_details)
                        .setTitle("删除提示")
                        .setMessage("确认清空下载目录中的文件？")
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (files!=null){
                                            for (File file : files) {
                                                file.delete();
                                            }
                                            data.clear();
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
            }
        });
        View contentView = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.history_item_delete_window, null);
        Button editButton = contentView.findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.setRestoreCheckBox(false);
                adapter.setCanSelectMore(true);
                adapter.notifyDataSetInvalidated();
                selectMoreBar.setVisibility(View.VISIBLE);
                deleteWindow.dismiss();
            }
        });
        Button deleteButton = contentView.findViewById(R.id.deleteButton1);
        deleteButton.setText("删除该文件");
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                data.remove(selectedPosition);
                adapter.notifyDataSetChanged();
                deleteWindow.dismiss();
            }
        });
        deleteWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteWindow.setFocusable(true);
        deleteWindow.setOutsideTouchable(true);
        deleteWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        confirmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("rer","selectedItemList:"+selectedItemList);
                for (int i = 0; i < selectedItemList.size(); i++) {
                    data.remove((int)selectedItemList.get(i));
                }
                adapter.setRestoreCheckBox(true);
                adapter.setCanSelectMore(false);
                adapter.notifyDataSetInvalidated();
                adapter.notifyDataSetChanged();
                selectMoreBar.setVisibility(View.GONE);
                selectedItemList.clear();
            }
        });
        cancelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.setCanSelectMore(false);
                adapter.setRestoreCheckBox(true);
                adapter.notifyDataSetInvalidated();
                selectMoreBar.setVisibility(View.GONE);
            }
        });
    }

    private void initData(){
        data.clear();
        File downloadPath = new File("/storage/emulated/0/DaintyDownloads");
        if (downloadPath.exists()){
            files= downloadPath.listFiles();
            if (files!=null){
                for (int i=files.length-1;i>=0;i--){
                    Map<String,Object> fileInfo=new HashMap<>();
                    fileInfo.put("file_name",files[i].getName());
                    fileInfo.put("file_size",Formatter.formatFileSize(this,files[i].length()));
                    fileInfo.put("last_modified",files[i].lastModified());
                    fileInfo.put("file_suffix",getFileType(files[i].getName()));
                    fileInfo.put("file_path",files[i].getAbsoluteFile());
                    data.add(fileInfo);
                }
            }
        }

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isDownloading)
            unregisterReceiver(downloadStatus);
    }

    /*
    每隔一秒发送一次广播
     */
    private BroadcastReceiver downloadStatus = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("download_rec","收到广播");
            if (intent.getBooleanExtra("finish_download",false)) {
                timer.cancel();
                downloadContainer.setVisibility(View.GONE);
                initData();
                adapter.notifyDataSetChanged();
            }else {
                downloadProgress.setProgress(intent.getIntExtra("current_download_size",0));
            }
        }
    };
    /**
     * 根据后缀获取文件fileName的类型
     * @return String 文件的类型
     **/
    public String getFileType(String fileName){
        if(!fileName.equals("")&&fileName.length()>3){
            int dot=fileName.lastIndexOf(".");
            if(dot>0){
                return fileName.substring(dot+1);
            }else{
                return "";
            }
        }
        return "";
    }
    private Intent getFileIntent(File file) {
        Uri uri = Uri.fromFile(file);
        String type = getMIMEType(file);
        Log.i("tag", "type=" + type);
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, type);
        return intent;
    }

    private String getMIMEType(File f) {
        String type;
        String fName = f.getName();
      /* 取得扩展名 */
        String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();

      /* 依扩展名的类型决定MimeType */
        switch (end) {
            case "pdf":
                type = "application/pdf";//
                break;

            case "m4a":
            case "mp3":
            case "mid":
            case "xmf":
            case "ogg":
            case "wav":
                type = "audio/*";
                break;

            case "3gp":
            case "mp4":
                type = "video/*";
                break;

            case "jpg":
            case "gif":
            case "png":
            case "jpeg":
            case "bmp":
                type = "image/*";
                break;

            case "apk":
        /* android.permission.INSTALL_PACKAGES */
                type = "application/vnd.android.package-archive";
                break;

            default:
        /*如果无法直接打开，就跳出软件列表给用户选择 */
                type = "*/*";
                break;
        }
        return type;
    }

    private static class UpdateDownloadSpeedHandler extends Handler{
        private WeakReference<DownloadRecordActivity> mWeakReference;
        private UpdateDownloadSpeedHandler(DownloadRecordActivity activity){
            mWeakReference=new WeakReference<>(activity);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DownloadRecordActivity activity=mWeakReference.get();
            if (activity!=null)
                activity.downloadSpeed.setText(Formatter.formatFileSize(activity,(long)msg.obj)+"/s");
        }
    }
    private void showNetSpeed() {

        long nowTotalRxBytes = getTotalRxBytes();
        Log.d("uuu",nowTotalRxBytes+"b");
        long speed = nowTotalRxBytes - lastTotalRxBytes;

        lastTotalRxBytes = nowTotalRxBytes;

        Message msg = mHandler.obtainMessage();
        msg.what = 100;
        msg.obj = speed ;

        mHandler.sendMessage(msg);//更新界面
    }
    private long getTotalRxBytes() {
        return TrafficStats.getUidRxBytes(getApplicationInfo().uid);
    }
}
