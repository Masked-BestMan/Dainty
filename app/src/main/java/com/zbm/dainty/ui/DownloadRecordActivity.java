package com.zbm.dainty.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zbm.dainty.adapter.DownloadRecordAdapter;
import com.zbm.dainty.R;
import com.zbm.dainty.bean.FileDownloadBean;
import com.zbm.dainty.task.DownloaderTask;
import com.zbm.dainty.util.DaintyDBHelper;
import com.zbm.dainty.util.DownloadHelper;
import com.zbm.dainty.util.FileUtil;
import com.zbm.dainty.util.MyUtil;
import com.zbm.dainty.widget.SwipeBackActivity;
import com.zbm.dainty.widget.TextProgressBar;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

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
    private Set<Integer> selectedItemList = new TreeSet<>();
    private int selectedPosition;
    private PopupWindow deleteWindow;
    private Timer timer;
    private File[] files;    //下载目录内的文件
    private int downloadingCount = 0;  //正在下载的文件数
    private Map<String, FileDownloadBean> pauseList = new LinkedHashMap<>();   //暂停任务列表
    private List<String> pauseListRemoveLog = new ArrayList<>();   //记录从pauseList移除的文件下载地址
    //private boolean flag=false;   //删除标志

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //startService(new Intent(this, FileListenerService.class));
        setContentView(R.layout.activity_download_record);
//        IntentFilter mFilter = new IntentFilter();
//        mFilter.addAction("download_progress_refresh");
        //LocalBroadcastManager.getInstance(this).registerReceiver(downloadStatus, mFilter);

        ButterKnife.bind(this);
        initData();
        initView();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                showNetSpeed();
            }
        }, 0, 1000);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //stopService(new Intent(this,FileListenerService.class));
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(downloadStatus);
        if (timer != null) {
            timer.cancel();
        }
        for (FileDownloadBean bean : pauseList.values()) {
            DaintyDBHelper.getDaintyDBHelper(this).updateDownloadTable(bean.getDownloadUrl()
                    , bean.getFilePath(), bean.getFileName(), bean.getFileSize(), bean.getDownloadProgress(),
                    bean.getLastModified());
        }
        pauseList.clear();
        for (String url : pauseListRemoveLog) {
            DaintyDBHelper.getDaintyDBHelper(this).deleteTableItem(DaintyDBHelper.DTB_NAME,
                    "where downloadUrl='" + url + "'");
        }
        DaintyDBHelper.getDaintyDBHelper(this).removeMessage();
    }

    @SuppressWarnings("ConstantConditions")
    private void initView() {
        adapter = new DownloadRecordAdapter(this, data);
        downloadRecordList.setAdapter(adapter);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        downloadRecordBarTheme.setBackgroundColor(Color.parseColor(preferences.getString("theme_color",
                "#474747")));

        downloadRecordBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        downloadRecordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (adapter.isCanSelectMore()) {
                    CheckBox itemCheckBox = view.findViewById(R.id.download_record_delete_checkbox);
                    if (itemCheckBox.isChecked()) {
                        itemCheckBox.setChecked(false);

                    } else {
                        itemCheckBox.setChecked(true);
                    }
                } else {
                    final FileDownloadBean fileDownloadBean = data.get(position);
                    if (fileDownloadBean.isFinished()) {
                        Intent intent = FileUtil.getFileIntent(new File(data.get(position).getFilePath()));
                        intent.putExtra("file_path", data.get(position).getFilePath());
                        startActivity(intent);

                    } else {
                        final ImageView downloadStatus = view.findViewById(R.id.download_status);
                        final TextView downloadSpeed = view.findViewById(R.id.download_speed);

                        if (!fileDownloadBean.isDownloading()) {

                            if (DownloadHelper.downloadList.size() == DownloadHelper.downloadLimitCount) {
                                Toast.makeText(DownloadRecordActivity.this, "下载数达最大限制",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            downloadStatus.setImageDrawable(getResources().getDrawable(R.drawable.stop_download));
                            downloadSpeed.setText(fileDownloadBean.getSpeed());
                            fileDownloadBean.setDownloading(true);
                            if (new File(fileDownloadBean.getFilePath()).exists())
                                fileDownloadBean.setDownloadProgress(pauseList.get(fileDownloadBean.getDownloadUrl()).getDownloadProgress());
                            else
                                fileDownloadBean.setDownloadProgress(0);
                            DownloaderTask downloaderTask = new DownloaderTask(DownloadRecordActivity.this,
                                    fileDownloadBean.getFileName(), new File(fileDownloadBean.getFilePath()),
                                    fileDownloadBean.getFileSize(), fileDownloadBean.getDownloadProgress());
                            downloaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, fileDownloadBean.getDownloadUrl());
                            DownloadHelper.downloadList.add(downloaderTask);
                            pauseListRemoveLog.add(fileDownloadBean.getDownloadUrl());
                            pauseList.remove(fileDownloadBean.getDownloadUrl());

                        } else {

                            fileDownloadBean.setDownloading(false);
                            downloadStatus.setImageDrawable(getResources().getDrawable(R.drawable.start_download));
                            downloadSpeed.setText("暂停");
                            DownloaderTask downloaderTask = DownloadHelper.getDownloadFile(fileDownloadBean.getFilePath());
                            if (downloaderTask == null) return;
                            Log.d("ewe", "task进度:" + downloaderTask.getProgress() + "bean进度：" + fileDownloadBean.getDownloadProgress());
                            fileDownloadBean.setDownloadUrl(downloaderTask.getDownloadUrl());
                            downloaderTask.setPause(true);
                            downloaderTask.cancel(true);
                            DownloadHelper.downloadList.remove(downloaderTask);
                            pauseList.put(fileDownloadBean.getDownloadUrl(), fileDownloadBean);
                            pauseListRemoveLog.remove(fileDownloadBean.getDownloadUrl());
                        }
                    }
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
                    deleteWindow.showAtLocation(view, Gravity.TOP | Gravity.END,
                            MyUtil.dip2px(DownloadRecordActivity.this, 20),
                            positions[1] + MyUtil.dip2px(DownloadRecordActivity.this, 60));
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
                    selectedItemList.remove(position);
                }
            }
        });
        emptyRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyUtil.createDialog(DownloadRecordActivity.this, "删除提示",
                        "清空后需要重新下载文件!", "确定"
                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DaintyDBHelper.getDaintyDBHelper(DownloadRecordActivity.this).deleteTableItem(DaintyDBHelper.DTB_NAME, null);
                                DownloadHelper.stopAllDownloads();
                                File savePath = new File("/storage/emulated/0/DaintyDownloads");
                                if (savePath.exists()) {
                                    for (File file : savePath.listFiles()) {
                                        file.delete();
                                    }
                                    initData();
                                }
                            }
                        }, null);
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
                deleteWindow.dismiss();
                //flag=true;
                final FileDownloadBean selectedBean = data.get(selectedPosition);
                //检查要删除的文件是否是暂停下载的
                if (pauseList.containsKey(selectedBean.getDownloadUrl())) {
                    DaintyDBHelper.getDaintyDBHelper(DownloadRecordActivity.this).deleteTableItem(DaintyDBHelper.DTB_NAME,
                            "where downloadUrl='" + selectedBean.getDownloadUrl() + "'");
                    pauseList.remove(selectedBean.getDownloadUrl());
                    pauseListRemoveLog.add(selectedBean.getDownloadUrl());
                    new File(selectedBean.getFilePath()).delete();
                    data.remove(selectedPosition);
                    adapter.notifyDataSetChanged();
                    refreshStorageStatus();
                } else {
                    final DownloaderTask task = DownloadHelper.getDownloadFile(selectedBean.getFilePath());
                    Log.d("Record", "任务：" + task);
                    if (task != null) {
                        MyUtil.createDialog(DownloadRecordActivity.this,"删除提示","删除下载中的文件需要重新下载!","仍要删除",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                task.cancel(true);
                                new File(selectedBean.getFilePath()).delete();
                                data.remove(selectedPosition);
                                adapter.notifyDataSetChanged();
                                refreshStorageStatus();
                                //flag=false;
                            }
                        },null);
                    } else {
                        new File(selectedBean.getFilePath()).delete();
                        data.remove(selectedPosition);
                        adapter.notifyDataSetChanged();
                        refreshStorageStatus();
                    }
                }
//                if (flag) {
//                    new File(selectedBean.getFilePath()).delete();
//                    data.remove(selectedPosition);
//                    adapter.notifyDataSetChanged();
//                    refreshStorageStatus();
//                }
            }
        });
        deleteWindow = new PopupWindow(contentView, MyUtil.dip2px(this, 120),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteWindow.setFocusable(true);
        deleteWindow.setOutsideTouchable(true);
        deleteWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        confirmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("rer", "选中：" + selectedItemList + "data size：" + data.size());
                for (int i : selectedItemList) {
                    FileDownloadBean selectedBean = data.get(i);
                    if (pauseList.containsKey(selectedBean.getDownloadUrl())) {
                        DaintyDBHelper.getDaintyDBHelper(DownloadRecordActivity.this).deleteTableItem(DaintyDBHelper.DTB_NAME,
                                "where downloadUrl='" + selectedBean.getDownloadUrl() + "'");
                        pauseList.remove(selectedBean.getDownloadUrl());
                        pauseListRemoveLog.add(selectedBean.getDownloadUrl());

                    }
                    new File(selectedBean.getFilePath()).delete();
                    DownloaderTask task = DownloadHelper.getDownloadFile(selectedBean.getFilePath());
                    if (task != null) {
                        task.cancel(true);
                    }
                    pauseListRemoveLog.add(selectedBean.getDownloadUrl());
                    pauseList.remove(selectedBean.getDownloadUrl());
                }
                int removeNum = -1;
                for (int i : selectedItemList) {
                    data.remove((i - (removeNum + 1)));
                    removeNum++;
                }
                adapter.setRestoreCheckBox(true);
                adapter.setCanSelectMore(false);
                adapter.notifyDataSetChanged();
                selectMoreBar.setVisibility(View.INVISIBLE);
                storageSizeBar.setVisibility(View.VISIBLE);
                selectedItemList.clear();
                refreshStorageStatus();
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
    }

    private void initData() {
        downloadingCount = DownloadHelper.downloadList.size();
        data.clear();
        pauseList.clear();
        pauseListRemoveLog.clear();

        DaintyDBHelper.getDaintyDBHelper(this).searchDownloadTable("select * from "
                        + DaintyDBHelper.DTB_NAME + " order by downloadTIME desc",
                new DaintyDBHelper.OnSearchDownloadTableListener() {
                    @Override
                    public void onResult(ArrayList<FileDownloadBean> mDownloadData) {
                        Log.d("download", "数据库：" + mDownloadData.size());
                        for (FileDownloadBean bean : mDownloadData)
                            pauseList.put(bean.getDownloadUrl(), bean);
                        data.addAll(mDownloadData);
                        File savePath = new File("/storage/emulated/0/DaintyDownloads");
                        if (savePath.exists()) {
                            files = savePath.listFiles();
                            if (files != null) {
                                for (File file : files) {
                                    if (file.isDirectory()) continue;
                                    if (data.contains(new FileDownloadBean(file.getName())))
                                        continue;
                                    FileDownloadBean fileInfo = new FileDownloadBean(file.getName());
                                    DownloaderTask task = DownloadHelper.getDownloadFile(file.getAbsolutePath());
                                    if (task != null) {
                                        fileInfo.setDownloading(true);
                                        fileInfo.setFinished(false);
                                        fileInfo.setLastModified(task.getTime());
                                        fileInfo.setDownloadProgress(task.getProgress());
                                        fileInfo.setFileSize(task.getFileSize());
                                        fileInfo.setDownloadUrl(task.getDownloadUrl());
                                    } else {
                                        fileInfo.setDownloading(false);
                                        fileInfo.setFinished(true);
                                        fileInfo.setLastModified(file.lastModified());
                                        fileInfo.setFileSize((int) file.length());
                                    }
                                    fileInfo.setFilePath(file.getAbsolutePath());
                                    data.add(fileInfo);
                                }

                            }
                            Collections.sort(data);
                            if (adapter != null)
                                adapter.notifyDataSetChanged();
                        }
                    }
                });

        refreshStorageStatus();
    }

    @SuppressLint("SetTextI18n")
    private void showNetSpeed() {
        if (downloadRecordList.getChildCount() != 0 && data.size() != 0) {
            downloadingCount = DownloadHelper.downloadList.size();
            int downloadItemInList = 0;
            final int childIndex = downloadRecordList.getFirstVisiblePosition();
            for (int i = downloadRecordList.getFirstVisiblePosition(); i <= downloadRecordList.getLastVisiblePosition(); i++) {
                if (i >= data.size()) return;
                if (data.get(i).isDownloading()) {
                    downloadItemInList++;
                    if (downloadItemInList > downloadingCount) break;
                    final int j = i;

                    DownloaderTask task = DownloadHelper.getDownloadFile(data.get(i).getFilePath());

                    if (task == null) return;
                    final int progress = task.getProgress();
                    if (progress != 0 && progress != data.get(j).getDownloadProgress()) {
                        View view = downloadRecordList.getChildAt(i - childIndex);
                        final ProgressBar progressBar = view.findViewById(R.id.download_progress);
                        final TextView speed = view.findViewById(R.id.download_speed);
                        Log.d("download", "task进度：" + progress + "bean进度：" + data.get(j).getDownloadProgress());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(progress);
                                String downloadSpeed = Formatter.formatFileSize(DownloadRecordActivity.this, progress - data.get(j).getDownloadProgress()) + "/s";
                                speed.setText(downloadSpeed);
                                data.get(j).setDownloadProgress(progress);
                                data.get(j).setSpeed(downloadSpeed);
                            }
                        });
                    }
                }
            }
        }
    }

    private void refreshStorageStatus() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long blockSize = statFs.getBlockSizeLong();
            long totalBlocks = statFs.getBlockCountLong();
            long availableBlocks = statFs.getAvailableBlocksLong();
            String totalSize = Formatter.formatFileSize(this, blockSize * totalBlocks);
            String availableSize = Formatter.formatFileSize(this, blockSize * availableBlocks);
            textProgressBar.setTextAndProgress("内置存储可用：" + availableSize + "/共：" + totalSize, (int) ((float) availableBlocks / totalBlocks * 100));
        } else {
            textProgressBar.setTextAndProgress("内置存储不可用", 0);
        }
    }

//    /*
//    每隔一秒发送一次广播
//     */
//    private BroadcastReceiver downloadStatus = new BroadcastReceiver() {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getBooleanExtra("finish_download", false)) {
//                initData();
//            }
//        }
//    };

}
