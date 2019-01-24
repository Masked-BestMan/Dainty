package com.zbm.dainty.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.zbm.dainty.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LoadingDialog extends Dialog {
    private RotateAnimation mRotateAnimation;
    private TextView tvLoading;
    private ImageView ivLoading;
    private View decorView;
    private String title;
    private ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture future;

    public LoadingDialog(@NonNull Context context){
        this(context,null);
    }

    public LoadingDialog(@NonNull Context context,@Nullable String title){
        super(context,R.style.LoadingDialog);
        this.title=title;
        setCancelable(false);
        scheduledExecutorService= Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        future=scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                decorView.post(new Runnable() {
                    @Override
                    public void run() {
                        LoadingDialog.this.dismiss();
                    }
                });
            }
        },20,TimeUnit.SECONDS);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (future!=null){
            future.cancel(false);
        }
    }

    private void initView() {
        // 设置窗口大小
        setContentView(R.layout.loading_with_title);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);
        decorView=getWindow().getDecorView();
        onBindView();
    }

    @Override
    public void show() {
        super.show();
        tvLoading.setText(title);
        ivLoading.startAnimation(mRotateAnimation);
    }

    @Override
    public void dismiss() {
        if (isShowing()) {
            ivLoading.clearAnimation();
            super.dismiss();
        }
    }

    public void setLoadingTitle(String title){
        this.title=title;
    }

    @Override
    public boolean isShowing() {
        return super.isShowing();
    }


    private void onBindView() {
        tvLoading = findViewById(R.id.tv_loading_title);
        ivLoading = findViewById(R.id.iv_loading);

        if (!TextUtils.isEmpty(title)) {
            tvLoading.setText(title);
        }else {
            tvLoading.setVisibility(View.GONE);
        }
        ivLoading.measure(0,0);
        mRotateAnimation = new RotateAnimation(0,360,ivLoading.getMeasuredWidth()/2,ivLoading.getMeasuredHeight()/2);
        mRotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation.setDuration(1000);
        mRotateAnimation.setRepeatCount(-1);
    }
}
