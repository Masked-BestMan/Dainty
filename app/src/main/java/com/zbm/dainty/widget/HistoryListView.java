package com.zbm.dainty.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.zbm.dainty.util.IDockingController;
import com.zbm.dainty.util.IDockingHeaderUpdateListener;

/**
 * Created by Zbm阿铭 on 2017/11/16.
 */

public class HistoryListView extends ExpandableListView implements AbsListView.OnScrollListener{
    private View mDockingHeader;
    private int mDockingHeaderWidth,mDockingHeaderHeight;
    private boolean mDockingHeaderVisible;
    private int mDockingHeaderState= IDockingController.DOCKING_HEADER_HIDDEN;

    private IDockingHeaderUpdateListener mListener;

    public HistoryListView(Context context) {
        this(context,null);
    }

    public HistoryListView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HistoryListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnScrollListener(this);
    }
    public void setDockingHeader(View header,IDockingHeaderUpdateListener listener){
        mDockingHeader=header;
        mListener=listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(mDockingHeader!=null){
            measureChild(mDockingHeader,widthMeasureSpec,heightMeasureSpec);
            mDockingHeaderWidth=mDockingHeader.getMeasuredWidth();
            mDockingHeaderHeight=mDockingHeader.getMeasuredHeight();
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(mDockingHeader!=null){
            mDockingHeader.layout(0,0,mDockingHeaderWidth,mDockingHeaderHeight);
        }
    }

    /*
        调用完onDraw后会调用此方法，header view只是画上去，不作为list view的child组成
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if(mDockingHeaderVisible){
            drawChild(canvas,mDockingHeader,getDrawingTime());
        }
    }

    @Override
    public void setAdapter(ExpandableListAdapter adapter) {
        super.setAdapter(adapter);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        long packedPosition=getExpandableListPosition(firstVisibleItem);
        int groupPosition=getPackedPositionGroup(packedPosition);
        int childPosition=getPackedPositionChild(packedPosition);  //如果该组没有child，childPosition=-1
        updateDockingHeader(groupPosition,childPosition);
    }
    private void updateDockingHeader(int groupPosition,int childPosition){
        if(getExpandableListAdapter()==null)return;
        if(getExpandableListAdapter() instanceof IDockingController){
            IDockingController dockingController=(IDockingController)getExpandableListAdapter();
            mDockingHeaderState=dockingController.getDockingState(groupPosition,childPosition);
            switch (mDockingHeaderState){
                case IDockingController.DOCKING_HEADER_HIDDEN:
                    mDockingHeaderVisible = false;
                    break;


                case IDockingController.DOCKING_HEADER_DOCKED:
                    if (mListener != null) {
                        mListener.onUpdate(mDockingHeader, groupPosition, isGroupExpanded(groupPosition));
                    }
                    // Header view might be "GONE" status at the beginning, so we might not be able
                    // to get its width and height during initial measure procedure.
                    // Do manual measure and layout operations here.
                    mDockingHeader.measure(
                            MeasureSpec.makeMeasureSpec(mDockingHeaderWidth, MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(mDockingHeaderHeight, MeasureSpec.EXACTLY));
                    mDockingHeader.layout(0, 0, mDockingHeaderWidth, mDockingHeaderHeight);
                    mDockingHeaderVisible = true;
                    break;

                /*
                    当滑动到第一个可视child是某个组最后一个child成员时的状态
                 */
                case IDockingController.DOCKING_HEADER_DOCKING:
                    if (mListener != null) {
                        mListener.onUpdate(mDockingHeader, groupPosition, isGroupExpanded(groupPosition));
                    }

                    View firstVisibleView = getChildAt(0);  //获取第一个可视child对象
                    int yOffset;
                    if (firstVisibleView.getBottom() < mDockingHeaderHeight) {
                        yOffset = firstVisibleView.getBottom() - mDockingHeaderHeight;
                    } else {
                        yOffset = 0;
                    }

                    // The yOffset is always non-positive. When a new header view is "docking",
                    // previous header view need to be "scrolled over". Thus we need to draw the
                    // old header view based on last child's scroll amount.
                    mDockingHeader.measure(
                            MeasureSpec.makeMeasureSpec(mDockingHeaderWidth, MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(mDockingHeaderHeight, MeasureSpec.EXACTLY));
                    mDockingHeader.layout(0, yOffset, mDockingHeaderWidth, mDockingHeaderHeight + yOffset);
                    mDockingHeaderVisible = true;
                    break;
            }
        }
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN && mDockingHeaderVisible) {
            Rect rect = new Rect();
            mDockingHeader.getDrawingRect(rect);
            if (rect.contains((int)ev.getX(), (int)ev.getY())
                    && mDockingHeaderState == IDockingController.DOCKING_HEADER_DOCKED) {
                // 点击 header view 区域,拦截事件
                return true;
            }
        }

        return super.onInterceptTouchEvent(ev);
    }

    // Note: As header view is drawn to the canvas instead of adding into view hierarchy,
    // it's useless to set its touch or click event listener. Need to handle these input
    // events carefully by ourselves.
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mDockingHeaderVisible) {
            Rect rect = new Rect();
            mDockingHeader.getDrawingRect(rect);

            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (rect.contains((int)ev.getX(), (int)ev.getY())) {
                        // 阻止事件被list view的item消费
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    long flatPosition = getExpandableListPosition(getFirstVisiblePosition());
                    int groupPos = ExpandableListView.getPackedPositionGroup(flatPosition);
                    if (rect.contains((int)ev.getX(), (int)ev.getY()) &&
                            mDockingHeaderState == IDockingController.DOCKING_HEADER_DOCKED) {
                        // handle header view click event (do group expansion & collapse)
                        if (isGroupExpanded(groupPos)) {
                            collapseGroup(groupPos);
                        } else {
                            expandGroup(groupPos);
                        }
                        return true;
                    }
                    break;
            }
        }

        return super.onTouchEvent(ev);
    }
}
