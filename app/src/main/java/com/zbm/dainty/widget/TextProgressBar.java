package com.zbm.dainty.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;


public class TextProgressBar extends ProgressBar {
    private String text="";
    private Paint mPaint;
    private int fontSize;
    public TextProgressBar(Context context) {
        this(context,null);
    }

    public TextProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TextProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float fontScale=context.getResources().getDisplayMetrics().scaledDensity;
        fontSize= (int) (15*fontScale+0.5f);
        initText();
    }

    private void initText(){
        mPaint = new Paint();
        mPaint.setTextSize(fontSize);
        mPaint.setColor(Color.GRAY);
    }

    public synchronized void setTextAndProgress(String text,int progress) {
        this.text=text;
        setProgress(progress);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect rect = new Rect();
        mPaint.getTextBounds(text, 0, this.text.length(), rect);
        int x = (getWidth() / 2) - rect.centerX();
        int y = (getHeight() / 2) - rect.centerY();
        canvas.drawText(this.text, x, y, this.mPaint);
    }

}
