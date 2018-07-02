package com.zbm.dainty.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.file.zip.ZipEntry;
import com.file.zip.ZipFile;
import com.zbm.dainty.R;
import com.zbm.dainty.adapter.CompressionListAdapter;
import com.zbm.dainty.task.ResolveFileContentTask;
import com.zbm.dainty.task.ZipRarExtractorTask;
import com.zbm.dainty.util.FileUtil;
import com.zbm.dainty.widget.SwipeBackActivity;


import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;

public class DecompressionActivity extends SwipeBackActivity {
    @BindView(R.id.decompression_manager_bar_theme)
    View decompressionManagerBarTheme;
    @BindView(R.id.decompression_manager_back)
    Button decompressionManagerBack;
    @BindView(R.id.title_file)
    TextView title;
    @BindView(R.id.decompression_file_list)
    ListView decompressionFileList;
    @BindView(R.id.decompression_manager_unzip)
    TextView decompressionButton;

    private ProgressDialog mDialog;
    private CompressionListAdapter adapter;
    private String handleFilePath;
    private List<Object> data = new ArrayList<>();
    private String type;
    private MyHandler handler;

    private static class MyHandler extends Handler {
        WeakReference<DecompressionActivity> reference;

        MyHandler(DecompressionActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1 && reference.get() != null) {
                reference.get().mDialog.dismiss();
                reference.get().adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decompression);
        Intent intent = getIntent();
        handleFilePath = intent.getStringExtra("file_path");
        ButterKnife.bind(this);

        initData();
        initView();
    }


    private void initData() {
        mDialog=new ProgressDialog(this);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setTitle("正在解析压缩包");
        mDialog.setMessage("请稍等...");
        mDialog.show();
        switch (FileUtil.getExtensionName(handleFilePath)) {
            case "zip":
                type = "zip";
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        ZipFile zipFile = null;
                        try {
                            zipFile = new ZipFile(handleFilePath, "GBK");
                            Enumeration<ZipEntry> entries = zipFile.getEntries();
                            while (entries.hasMoreElements()) {
                                ZipEntry entry = entries.nextElement();
                                if (!entry.isDirectory()) {
                                    data.add(entry);
                                }
                            }
                            handler.sendEmptyMessage(1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (zipFile != null) {
                                try {
                                    zipFile.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }).start();

                break;
            case "rar":
                type = "rar";
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Archive rarFile = null;
                        try {
                            rarFile = new Archive(new File(handleFilePath));
                            FileHeader fh = rarFile.nextFileHeader();
                            while (fh != null) {

                                if (!fh.isDirectory()) {
                                    data.add(fh);
                                }
                                fh = rarFile.nextFileHeader();
                            }
                            handler.sendEmptyMessage(1);
                        } catch (RarException | IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (rarFile != null) {
                                try {
                                    rarFile.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }
                }).start();

                break;
        }
        adapter = new CompressionListAdapter(this, data);
        handler = new MyHandler(this);
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        title.setText(handleFilePath);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        decompressionManagerBarTheme.setBackgroundColor(Color.parseColor(preferences.getString("theme_color",
                "#474747")));
        decompressionManagerBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        decompressionFileList.setAdapter(adapter);
        decompressionFileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new ResolveFileContentTask(DecompressionActivity.this, handleFilePath, type)
                        .execute(data.get(position));
            }
        });

        decompressionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ZipRarExtractorTask(DecompressionActivity.this, type, handleFilePath,
                        FileUtil.getDirFromPath(handleFilePath) + File.separator +
                                FileUtil.getFileNameNoEx(FileUtil.getFileNameFromPath(handleFilePath)),
                        false).execute();

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        File[] files = new File(getExternalCacheDir() + "/compression_temp/").listFiles();
        if (files != null)
            for (File file : files) {
                file.delete();
            }
    }
}
