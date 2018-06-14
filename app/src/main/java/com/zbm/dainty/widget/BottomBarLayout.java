package com.zbm.dainty.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class BottomBarLayout extends RelativeLayout {
    public BottomBarLayout(Context context) {
        super(context);
    }

    public BottomBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomBarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
