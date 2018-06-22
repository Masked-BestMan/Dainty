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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
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
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zbm.dainty.adapter.DownloadRecordAdapter;
import com.zbm.dainty.R;
import com.zbm.dainty.bean.FileDownloadBean;
import com.zbm.dainty.task.DownloaderTask;
import com.zbm.dainty.util.DownloadHelper;
import com.zbm.dainty.util.MyUtil;
import com.zbm.dainty.widget.SwipeBackActivity;
import com.zbm.dainty.widget.TextProgressBar;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    @BindView(R.id.download_record_list)
    ListView downloadRecordList;
    @BindView(R.id.download_record_storage_size_bar)
    View storageSizeBar;
    @BindView(R.id.storage_size_progress)
    TextProgressBar textProgressBar;
    @BindView(R.id.empty_download_record)
    TextView emptyRecord;
    @BindView(R.id.download_record_select_more_bar)
    View selectMoreBar;
    @BindView(R.id.download_record_confirm_delete)
    Button confirmDelete;
    @BindView(R.id.download_record_cancel_delete)
    Button cancelDelete;

    private DownloadRecordAdapter adapter;
    private List<FileDownloadBean> data = new ArrayList<>();
    private List<Integer> selectedItemList = new ArrayList<>();
    private int selectedPosition;
    private PopupWindow deleteWindow;
    private Timer timer;
    private File[] files;    //下载目录内的文件
    private int downloadCount = 0;
    private int[] firstDownloadLength;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction("download_progress_refresh");
        registerReceiver(downloadStatus, mFilter);
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


    @SuppressWarnings("ConstantConditions")
    private void initView() {

        adapter = new DownloadRecordAdapter(this, data);
        downloadRecordList.setAdapter(adapter);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        downloadRecordBarTheme.setBackgroundColor(Color.parseColor(preferences.getString("theme_color", "#474747")));

        downloadRecordBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        downloadRecordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter.isCanSelectMore()) {
                    CheckBox itemCheckBox = view.findViewById(R.id.download_record_delete_checkbox);
                    if (itemCheckBox.isChecked()) {
                        itemCheckBox.setChecked(false);

                    } else {
                        itemCheckBox.setChecked(true);
                    }
                } else {
                    startActivity(getFileIntent(new File(data.get(position).getFilePath())));
                }

            }
        });
        downloadRecordList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position;
                if (!selectMoreBar.isShown()) {
                    int[] positions = new int[2];
                    view.getLocationOnScreen(positions);
                    deleteWindow.showAtLocation(view, Gravity.TOP | Gravity.END, 50, positions[1] + MyUtil.dip2px(DownloadRecordActivity.this, 60));
                }
                return true;
            }
        });
        adapter.setOnCheckChangedListener(new DownloadRecordAdapter.OnCheckChangedListener() {
            @Override
            public void onCheckChanged(int position, boolean checked) {
                if (checked) {
                    selectedItemList.add(position);
                } else {
                    selectedItemList.remove((Integer) position);
                }
                Log.d("rer", "selected:" + selectedItemList);
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
                                        DownloadHelper.stopAllDownloads();
                                        if (files != null) {
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
        @SuppressLint("InflateParams")
        View contentView = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.history_item_delete_window, null);
        Button editButton = contentView.findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.setRestoreCheckBox(false);
                adapter.setCanSelectMore(true);
                adapter.notifyDataSetInvalidated();
                selectMoreBar.setVisibility(View.VISIBLE);
                storageSizeBar.setVisibility(View.INVISIBLE);
                deleteWindow.dismiss();
            }
        });
        Button deleteButton = contentView.findViewById(R.id.deleteButton1);
        deleteButton.setText("删除该文件");
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new File(data.get(selectedPosition).getFilePath()).delete();
                if (selectedPosition<=DownloadHelper.downloadList.size()-1){
                    DownloadHelper.downloadList.get(selectedPosition).cancel(true);
                    DownloadHelper.downloadList.remove(selectedPosition);
                }
                initData();
                adapter.notifyDataSetChanged();
                deleteWindow.dismiss();
            }
        });
        deleteWindow = new PopupWindow(contentView,  MyUtil.dip2px(this,120),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteWindow.setFocusable(true);
        deleteWindow.setOutsideTouchable(true);
        deleteWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        confirmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (int i = 0; i < selectedItemList.size(); i++) {
                    new File(data.get(selectedItemList.get(i)).getFilePath()).delete();
                    Log.d("rer", "删除路径:" + data.get(selectedItemList.get(i)).getFilePath());
                    if (selectedItemList.get(i)<=DownloadHelper.downloadList.size()-1){
                        DownloadHelper.downloadList.get(selectedItemList.get(i)).cancel(true);
                        DownloadHelper.downloadList.remove((int)selectedItemList.get(i));
                    }
                }
                initData();
                adapter.setRestoreCheckBox(true);
                adapter.setCanSelectMore(false);
                adapter.notifyDataSetInvalidated();
                adapter.notifyDataSetChanged();
                selectMoreBar.setVisibility(View.INVISIBLE);
                storageSizeBar.setVisibility(View.VISIBLE);
                selectedItemList.clear();
            }
        });
        cancelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.setCanSelectMore(false);
                adapter.setRestoreCheckBox(true);
                adapter.notifyDataSetInvalidated();
                selectMoreBar.setVisibility(View.INVISIBLE);
                storageSizeBar.setVisibility(View.VISIBLE);
            }
        });
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long blockSize = statFs.getBlockSizeLong();
            long totalBlocks = statFs.getBlockCountLong();
            long availableBlocks = statFs.getAvailableBlocksLong();
            String totalSize = Formatter.formatFileSize(this, blockSize * totalBlocks);
            String availableSize = Formatter.formatFileSize(this, blockSize * availableBlocks);
            textProgressBar.setTextAndProgress("内置存储可用：" + availableSize + "/共：" + totalSize, (int) ((float)availableBlocks / totalBlocks * 100));
        }
    }

    private void initData() {
        data.clear();
        downloadCount = DownloadHelper.downloadList.size();

        if (downloadCount > 0) {
            if (timer != null) timer.cancel();
            firstDownloadLength = new int[downloadCount];
            for (int i = 0; i < downloadCount; i++) {
                firstDownloadLength[i] = DownloadHelper.downloadList.get(i).getProgress();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    showNetSpeed();
                }
            }, 0, 1000);
        }
        File downloadPath = new File("/storage/emulated/0/DaintyDownloads");
        if (downloadPath.exists()) {
            files = downloadPath.listFiles();
            if (files != null) {
                for (File file : files) {
                    FileDownloadBean fileInfo = new FileDownloadBean(file.getName());
                    DownloaderTask task=DownloadHelper.getDownloadFile(file.getAbsolutePath());
                    if (task!=null) {
                        fileInfo.setDownloading(true);
                        fileInfo.setLastModified(task.getTime());
                    }else {
                        fileInfo.setDownloading(false);
                        fileInfo.setLastModified(file.lastModified());
                    }
                    fileInfo.setFileSize(Formatter.formatFileSize(this, file.length()));

                    fileInfo.setFileSuffix(getFileType(file.getName()));
                    fileInfo.setFilePath(file.getAbsolutePath());
                    data.add(fileInfo);
                }
                Collections.sort(data);

                Log.d("Dainty",data.toString());
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(downloadStatus);
    }

    /*
    每隔一秒发送一次广播
     */
    private BroadcastReceiver downloadStatus = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("download_rec", "收到广播");
            if (intent.getBooleanExtra("finish_download", false)) {
                initData();
                adapter.notifyDataSetChanged();
            }
        }
    };

    /**
     * 根据后缀获取文件fileName的类型
     *
     * @return String 文件的类型
     **/
    public String getFileType(String fileName) {
        if (!fileName.equals("") && fileName.length() > 3) {
            int dot = fileName.lastIndexOf(".");
            if (dot > 0) {
                return fileName.substring(dot + 1);
            } else {
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

    @SuppressLint("SetTextI18n")
    private void showNetSpeed() {

        if (downloadRecordList.getChildCount() != 0) {
            if (DownloadHelper.downloadList.size() == 0 || downloadCount != DownloadHelper.downloadList.size())
                return;
            int downloadItemInList = 0;
            if (downloadRecordList.getFirstVisiblePosition() < downloadCount) {
                Log.d("Dainty","第一个可见："+downloadRecordList.getFirstVisiblePosition());
                downloadItemInList = downloadCount - downloadRecordList.getFirstVisiblePosition();  //显示在列表中的下载条目数
                Log.d("Dainty","下载条目："+downloadItemInList);
            }
            for (int i = 0; i < downloadItemInList; i++) {
                final int j = i;
                final int progress = DownloadHelper.downloadList.get(i).getProgress();
                View view = downloadRecordList.getChildAt(i);
                final ProgressBar progressBar = view.findViewById(R.id.download_progress);
                final TextView speed = view.findViewById(R.id.download_speed);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(progress);
                        if (progress != firstDownloadLength[j])
                            speed.setText(Formatter.formatFileSize(DownloadRecordActivity.this, progress - firstDownloadLength[j]) + "/s");
                        firstDownloadLength[j] = progress;
                    }
                });

            }

        }
    }
}
