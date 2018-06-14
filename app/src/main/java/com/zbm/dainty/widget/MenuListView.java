package com.zbm.dainty.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * Created by zbm阿铭 on 2018/2/28.
 */

public class MenuListView extends ListView {
    private static int mMaxOverDistance = 50;
    private Context mContext;
    public MenuListView(Context context) {
        super(context);
        this.mContext = context;
        initView();
    }

    public MenuListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView();
    }

    public MenuListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    private void initView() {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        float density = metrics.density;
        mMaxOverDistance = (int) (density * mMaxOverDistance);
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY,
                                   int scrollX, int scrollY,
                                   int scrollRangeX, int scrollRangeY,
                                   int maxOverScrollX, int maxOverScrollY,
                                   boolean isTouchEvent) {
        return super.overScrollBy(deltaX, deltaY,
                scrollX, scrollY,
                scrollRangeX, scrollRangeY,
                maxOverScrollX, mMaxOverDistance, isTouchEvent);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);//Measure specification mode: The child can be as large as it wants up to the specified size.
        // ——>处理ScrollView嵌套ListView只显示一行的问题，此处让ListView所占的大小与要求的大小一样大
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
