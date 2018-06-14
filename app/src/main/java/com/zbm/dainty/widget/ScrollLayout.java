package com.zbm.dainty.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.OverScroller;
import android.widget.RelativeLayout;

import com.zbm.dainty.R;

/**
 * Created by zbm阿铭 on 2018/2/10.
 */

public class ScrollLayout extends RelativeLayout {
    private Context context;
    private View mTop;
    private int mTopViewHeight;
    private OverScroller mScroller;

    public ScrollLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        mScroller = new OverScroller(context);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //这个id必须能找到
        mTop = findViewById(R.id.toolbar);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTopViewHeight = mTop.getMeasuredHeight();

    }

    //这里留出状态栏的高度
    @Override
    public void scrollTo(int x, int y) {

        if (y < 0) {
            y = 0;
        }
        if (y > mTopViewHeight) {
            y = mTopViewHeight;
        }
        if (y != getScrollY()) {
            super.scrollTo(x, y);
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            invalidate();
        }
    }
}
