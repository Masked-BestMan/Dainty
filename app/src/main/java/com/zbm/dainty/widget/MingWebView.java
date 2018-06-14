package com.zbm.dainty.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.tencent.smtt.sdk.WebView;

/**
 * Created by Zbm阿铭 on 2018/1/23.
 */

public class MingWebView extends WebView implements GestureDetector.OnGestureListener{
    private float mFirstY;
    private OnScrollChangedCallback mOnScrollChangedCallback;
    private GestureDetector gestureDetector;

    public MingWebView(Context context) {
        this(context,null);
    }

    public MingWebView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet,0);
    }

    public MingWebView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        gestureDetector=new GestureDetector(context,this);
    }
/*
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mFirstY= (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                mLastY= (int) ev.getRawY();
                dy=mFirstY-mLastY;
                break;
        }

        //页面滑动到顶端，x5不能用getScrollY
        //if (getWebScrollY()==0&&dy!=0){
        if (Math.abs(mFirstY-mLastY)>touchSlop)
            mOnScrollChangedCallback.onScroll(0, dy);
        //}
        return super.onInterceptTouchEvent(ev);
    }
*/

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

//重写onScrollChanged
//    @Override
//    protected void onScrollChanged(final int l, final int t, final int oldl,
//                                   final int oldt) {
//        super.onScrollChanged(l, t, oldl, oldt);
//        //int dy=t-oldt;   //该值的正负不与滑动方向一致
//        Log.d("www","l - oldl, t - oldt"+(l - oldl)+", "+dy);
//        Log.d("www","isUpScroll"+isUpScroll);
//        if (mOnScrollChangedCallback != null) {
//            if ((isUpScroll&&dy>0)||(!isUpScroll&&dy<0)) {
//                mOnScrollChangedCallback.onScroll(l - oldl , dy);
//            }
//        }
//    }


    public void setOnScrollChangedCallback(
            final OnScrollChangedCallback onScrollChangedCallback) {
        mOnScrollChangedCallback = onScrollChangedCallback;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        mFirstY=e.getRawY();
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        float mLastY = e2.getRawY();
        boolean isUpScroll = mFirstY > mLastY;//是否上滑
        if (mOnScrollChangedCallback!=null){
            if ((isUpScroll&&distanceY>0)||(!isUpScroll&&distanceY<0))
                mOnScrollChangedCallback.onScroll((int)distanceX,(int)distanceY);
        }
        mFirstY= mLastY;
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    public interface OnScrollChangedCallback {
        void onScroll(int dx, int dy);
    }
}
