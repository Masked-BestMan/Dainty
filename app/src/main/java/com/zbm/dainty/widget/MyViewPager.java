package com.zbm.dainty.widget;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.FrameLayout;


import com.zbm.dainty.bean.WebDeleteEvent;
import com.zbm.dainty.util.WebPageHelper;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Zbm阿铭 on 2017/11/1.
 */

public class MyViewPager extends ViewPager implements OnGestureListener{
    private boolean isFullScreen=true;
    private OnLayoutClickListener lc;
    private GestureDetector gestureDetector;
    private boolean canDel=true;

    public MyViewPager(Context context) {
        this(context,null);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
            gestureDetector=new GestureDetector(context,this);
        this.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //防止viewpager在滚动中item仍可以上下滑动
                canDel = state == SCROLL_STATE_IDLE;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return !isFullScreen;
    }

    private FrameLayout frameLayout;
    protected float point_x, point_y; //手指按下的位置
    private int left, right, bottom;
    private int measureWidth,measureHeight;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                frameLayout= WebPageHelper.webpagelist.get(getCurrentItem()).getInnerContainer();
                measureWidth=frameLayout.getMeasuredWidth();
                measureHeight=frameLayout.getMeasuredHeight();

                point_x = ev.getRawX();
                point_y = ev.getRawY();
                left = frameLayout.getLeft();
                right = frameLayout.getRight();
                bottom = frameLayout.getBottom();
                break;
            case MotionEvent.ACTION_MOVE:
                float mov_x = ev.getRawX() - point_x;
                float mov_y = ev.getRawY() - point_y;
                Log.d("trr","mov_y"+mov_y);
                if(Math.abs(mov_x) < Math.abs(mov_y)&&canDel&&mov_y<frameLayout.getWidth()/4){
                    frameLayout.measure(MeasureSpec.makeMeasureSpec(measureWidth,MeasureSpec.AT_MOST),MeasureSpec.makeMeasureSpec(measureHeight,MeasureSpec.AT_MOST));
                    frameLayout.layout(left, (int) mov_y, right, bottom + (int) mov_y);
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d("trr","frameLayout:"+frameLayout
                        .getTop());
                if(Math.abs(frameLayout.getTop())>frameLayout.getWidth()/2){
                    EventBus.getDefault().post(new WebDeleteEvent(frameLayout.getTop()));

                }else {
                    ObjectAnimator objectAnimator=ObjectAnimator.ofFloat(frameLayout,"translationY",frameLayout.getTop(),0);
                    objectAnimator.setDuration(400).start();
                    frameLayout.measure(MeasureSpec.makeMeasureSpec(measureWidth,MeasureSpec.AT_MOST),MeasureSpec.makeMeasureSpec(measureHeight,MeasureSpec.AT_MOST));
                    frameLayout.layout(left,0,right,bottom);
                }
        }
        gestureDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }


    public void setFullScreen(boolean fullScreen) {
        isFullScreen = fullScreen;
    }

    public void setOnLayoutClickListener(OnLayoutClickListener lc){
        this.lc=lc;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        lc.onLayoutClick();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if(velocityY<-7000){
            EventBus.getDefault().post(new WebDeleteEvent(frameLayout.getTop()));
            return true;
        }
        if(Math.abs(frameLayout.getTop())>frameLayout.getWidth()/2){
            EventBus.getDefault().post(new WebDeleteEvent(frameLayout.getTop()));
        }else {
            frameLayout.layout(left,0,right,bottom);
        }
        return true;
    }
    public interface OnLayoutClickListener{
        void onLayoutClick();
    }

}
