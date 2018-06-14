package com.zbm.dainty.widget;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.TrafficStats;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.zbm.dainty.R;

import java.text.DecimalFormat;

/**
 * Created by Zbm阿铭 on 2017/12/25.
 */

public class PreferenceWithRightIcon extends Preference {
    private Context context;
    private long total_flow=0;
    private int type;     //默认0是普通的设置项，其他数字为带右标题流量框
    private String left,right;
    private DecimalFormat df=new DecimalFormat("#.00");

    public PreferenceWithRightIcon(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }
    public PreferenceWithRightIcon(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        this.context=context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PreferenceWithRightIcon);
        left = ta.getString(R.styleable.PreferenceWithRightIcon_left_title);
        right = ta.getString(R.styleable.PreferenceWithRightIcon_right_title);
        type = ta.getInt(R.styleable.PreferenceWithRightIcon_type,0);
        ta.recycle();
        if (type!=0){
            getNetFlow();
            String unit;
            double flow;
            if(total_flow<1024) {
                flow=total_flow;
                unit = "B";
            }else if (total_flow<Math.pow(1024,2)){
                flow=total_flow/1024.0;
                unit="KB";
            }else if(total_flow<Math.pow(1024,3)){
                flow=total_flow/Math.pow(1024,2);
                unit="MB";
            }else{
                flow=total_flow/Math.pow(1024,3);
                unit="GB";
            }
            right=df.format(flow)+unit;
        }
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        return LayoutInflater.from(getContext()).inflate(R.layout.pref_item_layout, parent, false);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView leftTitle = view.findViewById(R.id.left_title);
        leftTitle.setText(left);
        TextView rightTitle = view.findViewById(R.id.right_title);
        rightTitle.setText(right);
    }
    private void getNetFlow(){
        int uid=getPackageUid();
        if(uid!=-1)
            total_flow=TrafficStats.getUidRxBytes(uid) + TrafficStats.getUidTxBytes(uid);
    }
    private int getPackageUid(){
        try{
            PackageManager pm=context.getPackageManager();
            ApplicationInfo ai=pm.getApplicationInfo("com.zbm.dainty",PackageManager.GET_META_DATA);
            return ai.uid;
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        return -1;
    }
}
