package com.zbm.dainty.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by zbm阿铭 on 2018/3/16.
 */

//有一个问题,当我把它放在列表项中, 然后列表上面有一个edittext用来filter, 结果发现该自定义imageview本该被edittext挡住, 结果却绘制在edittext之上
//初步判断是由canvas.clipPath(path, Region.Op.REPLACE);引起的,它把第一次不存在的部分也画了
//解决方案: REPLACE(覆盖) 改为 INTERSECT(交集)

@SuppressLint("AppCompatCustomView")
public class CircleImageView extends ImageView {

    // 设置画布抗锯齿(毛边过滤)
    private PaintFlagsDrawFilter pfdf = null;
    private Path path = null;

    public CircleImageView(Context context) {
        super(context);
        init(context);
    }

    public CircleImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CircleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
//        Paint paint = new Paint();
//        // 透明度: 00%=FF（不透明） 100%=00（透明）
//        paint.setColor(Color.WHITE);
//        paint.setStyle(Paint.Style.STROKE);
//        // 解决图片拉伸后出现锯齿的两种办法: 1.画笔上设置抗锯齿 2.画布上设置抗锯齿
//        // http://labs.easymobi.cn/?p=3819
//        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
//        paint.setAntiAlias(true);    //设置线条等图形的抗锯齿
        int clearBits = 0;
        int setBits = Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG;
        pfdf = new PaintFlagsDrawFilter(clearBits, setBits);
        //由于imageview有默认底色,如黑色,设置背景为透明是为了第一次setImageBitmap时不显示圆以外方型的默认背景色
        //但是这样在中兴nubia手机上还会首先显示正方形黑色背景,然后才变圆(解决办法,先裁成圆再setImageBitmap)
        setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        // CCW: CounterClockwise(逆时针)
        // CW: Clockwise(顺时针)
        if (path == null) {
            path = new Path();
            path.addCircle(width / 2f, height / 2f, Math.min(width / 2f, height / 2f), Path.Direction.CCW);
            path.close();    //
        }
//      canvas.drawCircle(width / 2f, height / 2f, Math.min(width / 2f, height / 2f), paint);
        // super.onDraw里面也可能有多个canvas.save
        int saveCount = canvas.save();
        canvas.setDrawFilter(pfdf);    //设置图形、图片的抗锯齿。可用于线条等。
        // Region.Op.REPLACE 是显示第二次的
//      canvas.clipPath(path, Region.Op.REPLACE);
        canvas.clipPath(path, Region.Op.INTERSECT);
        super.onDraw(canvas);
        canvas.restoreToCount(saveCount);
    }
}
