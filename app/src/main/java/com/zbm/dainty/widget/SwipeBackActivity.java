package com.zbm.dainty.widget;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.zbm.dainty.R;


/**
 * Created by Zbm阿铭 on 2017/5/16.
 */

@SuppressLint("Registered")
public class SwipeBackActivity extends AppCompatActivity {
    private SwipeBackLayout mSwipeBackLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipeBackLayout = new SwipeBackLayout(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mSwipeBackLayout.attachActivity(this);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.right_out);
    }
    public void canSwipe(boolean allowable){
        mSwipeBackLayout.setCanTryCaptureView(allowable);
    }
}
