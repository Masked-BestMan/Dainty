package com.zbm.dainty.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.zbm.dainty.main.MainActivity;
import com.zbm.dainty.widget.CircleImageView;
import com.zbm.dainty.util.DaintyDBHelper;
import com.zbm.dainty.util.MyUtil;
import com.zbm.dainty.util.PictureUtil;
import com.zbm.dainty.R;
import com.zbm.dainty.widget.SwipeBackActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zbm阿铭 on 2018/3/13.
 */

public class CollectionEditActivity extends SwipeBackActivity {
    @BindView(R.id.collection_edit_bar_theme)
    View collectionEditBarTheme;
    @BindView(R.id.collection_edit_back)
    Button collectionEditBack;
    @BindView(R.id.collection_edit_confirm)
    TextView collectionEditConfirm;
    @BindView(R.id.collection_edit_icon)
    CircleImageView collectionEditIcon;
    @BindView(R.id.collection_edit_url)
    EditText collectionEditUrl;
    @BindView(R.id.collection_edit_title)
    EditText collectionEditTitle;
    @BindView(R.id.edit_to_collection)
    CheckBox editToCollection;
    @BindView(R.id.edit_to_desktop)
    CheckBox editToDesktop;

    private InputMethodManager inputMethodManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_edit);
        ButterKnife.bind(this);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        initView();
    }

    private void initView() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        collectionEditBarTheme.setBackgroundColor(Color.parseColor(preferences.getString("theme_color", "#474747")));
        final Bitmap icon = getIntent().getParcelableExtra("icon");
        final String title = getIntent().getStringExtra("title");
        final String url = getIntent().getStringExtra("url");

        collectionEditIcon.setImageBitmap(icon);
        collectionEditTitle.setText(title);
        collectionEditUrl.setText(url);
        collectionEditBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        collectionEditConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editToCollection.isChecked()) {
                    DaintyDBHelper.getDaintyDBHelper(CollectionEditActivity.this).updateCollectionTable(PictureUtil.bitmapToBytes(icon),url,title);
                }
                if (editToDesktop.isChecked()){
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
                        initPermission();
                    else {
                        addShortCut();
                    }
                }
                finish();
            }
        });
        editToCollection.setChecked(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (MyUtil.isSoftInputMethodShowing(this)) {
            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {
                Manifest.permission.INSTALL_SHORTCUT,
                Manifest.permission.UNINSTALL_SHORTCUT
        };

        ArrayList<String> toApplyList = new ArrayList<>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }else {
            addShortCut();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            boolean isAllGranted = true;

            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (!isAllGranted) {
                // 如果用户拒绝授权，则退出
                finish();
            }else {
                addShortCut();
            }
        }
    }
    public void addShortCut() {
        Intent sIntent = new Intent(Intent.ACTION_MAIN);
        sIntent.addCategory(Intent.CATEGORY_LAUNCHER);// 加入action,和category之后，程序卸载的时候才会主动将该快捷方式也卸载
        sIntent.setClass(this, MainActivity.class);//点击后进入的Activity
        sIntent.putExtra("shortcut_url",String.valueOf(collectionEditUrl.getText()));
        Intent installer = new Intent();
        installer.putExtra("duplicate", false);//false标示不重复创建
        installer.putExtra("android.intent.extra.shortcut.INTENT", sIntent);
        //设置快捷方式的名称
        installer.putExtra("android.intent.extra.shortcut.NAME", String.valueOf(collectionEditTitle.getText()));
        //设置图标
        installer.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", Intent.ShortcutIconResource.fromContext(this, R.drawable.collection_icon_default));
        installer.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        this.sendBroadcast(installer);//发送安装桌面图标的通知
    }

}
