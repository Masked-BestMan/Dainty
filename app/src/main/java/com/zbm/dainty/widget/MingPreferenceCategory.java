package com.zbm.dainty.widget;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zbm.dainty.R;

/**
 * Created by Zbm阿铭 on 2018/1/27.
 */

public class MingPreferenceCategory extends PreferenceCategory {
    private Context context;
    public MingPreferenceCategory(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }
    public MingPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        return LayoutInflater.from(context).inflate(R.layout.contacts_category,parent,false);
    }

}
