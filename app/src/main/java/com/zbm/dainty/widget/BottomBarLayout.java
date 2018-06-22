package com.zbm.dainty.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class BottomBarLayout extends LinearLayout {
    public BottomBarLayout(Context context) {
        this(context,null);
    }

    public BottomBarLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BottomBarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
