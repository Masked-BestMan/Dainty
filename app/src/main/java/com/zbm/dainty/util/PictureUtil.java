package com.zbm.dainty.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by zbm阿铭 on 2018/3/11.
 */

public class PictureUtil {
    //图片转为二进制数据
    public static byte[] bitmapToBytes(Bitmap bitmap){

        //将图片转化为位图
        ByteArrayOutputStream b= new ByteArrayOutputStream();
        try {
            //设置位图的压缩格式，质量为100%，并放入字节数组输出流中
            bitmap.compress(Bitmap.CompressFormat.PNG, 10, b);
            //将字节数组输出流转化为字节数组byte[]
            return b.toByteArray();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new byte[0];
    }

    public static Bitmap bytesToBitmap(@NonNull byte[] bytes){
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
