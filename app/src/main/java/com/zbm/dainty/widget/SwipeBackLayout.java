package com.zbm.dainty.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.ViewDragHelper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.zbm.dainty.R;

/**
 * Created by Zbm阿铭 on 2017/5/16.
 */

public class SwipeBackLayout extends FrameLayout {
    private ViewDragHelper mHelper;
    private Drawable mShadowLeft;
    private static final int FULL_ALPHA = 255;
    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;
    private float mScrimOpacity;
    private float mScrollPercent;
    private Rect mTmpRect = new Rect();
    private View mView;
    private Activity mActivity;
    private int mViewWidth;
    private boolean canTryCaptureView=true;

    public SwipeBackLayout(Context context) {
        super(context);
        init();
    }

    private void init() {
        mHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                // 默认捕获获 View
                return canTryCaptureView;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                // 拖动限制（大于左边界）
                return Math.max(0, left);
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                // 拖动距离大于屏幕的3分之1右移，拖动距离小于屏幕的3分之1左移
                int left = releasedChild.getLeft();
                if (left > getWidth() / 3) {
                    mActivity.finish();
                } else {
                    mHelper.settleCapturedViewAt(0, 0);
                }
                invalidate();
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                mScrollPercent = Math.abs((float) left / (mView.getWidth() + mShadowLeft.getIntrinsicWidth()));
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return mViewWidth;
            }
        });

        // 跟踪左边界拖动
        mHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
        setShadow();//设置侧滑的边框
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 拦截代理
        return mHelper.shouldInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Touch Event 代理
        mHelper.processTouchEvent(event);
        invalidate();
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        mScrimOpacity=1-mScrollPercent;
        // 子 View 需要更新状态
        if (mHelper.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mViewWidth=mView.getWidth();
    }

    public void setShadow() {

        mShadowLeft=getResources().getDrawable(R.drawable.shadow_left);

        invalidate();
    }
    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final boolean drawContent = child == mView;

        boolean ret = super.drawChild(canvas, child, drawingTime);
        if (drawContent && mHelper.getViewDragState() != ViewDragHelper.STATE_IDLE) {
            drawShadow(canvas, child);
            drawScrim(canvas, child);
        }
        return ret;
    }
    private void drawShadow(Canvas canvas, View child) {
        final Rect childRect = mTmpRect;
        child.getHitRect(childRect);
        mShadowLeft.setBounds(childRect.left - mShadowLeft.getIntrinsicWidth(), childRect.top,
                childRect.left, childRect.bottom);
        mShadowLeft.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
        mShadowLeft.draw(canvas);

    }

    private void drawScrim(Canvas canvas, View child) {
        int mScrimColor = DEFAULT_SCRIM_COLOR;
        final int baseAlpha = (mScrimColor & 0xff000000) >>> 24;
        final int alpha = (int) (baseAlpha * mScrimOpacity);
        final int color = alpha << 24 | (mScrimColor & 0xffffff);
        canvas.clipRect(0, 0, child.getLeft(), getHeight());
        canvas.drawColor(color);
    }
    /**
     * 绑定 Activity
     *
     * @param activity 容器 Activity
     */
    public void attachActivity(Activity activity) {
        mActivity = activity;
        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
        View content = decor.getChildAt(0);
        decor.removeView(content);
        mView = content;
        addView(content);
        decor.addView(this);
    }

    public void setCanTryCaptureView(boolean canTryCaptureView) {
        this.canTryCaptureView = canTryCaptureView;
    }
}
