package com.zbm.dainty.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.zbm.dainty.R;


/**
 * Created by Zbm阿铭 on 2018/1/11.
 */

public class PreferenceHead extends Preference {
    private Context context;
    private View.OnClickListener onBackButtonClickListener;
    public PreferenceHead(Context context,AttributeSet attrs){
        this(context,attrs,0);
    }
    public PreferenceHead(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context,attrs,defStyleAttr);
        this.context=context;
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        return LayoutInflater.from(getContext()).inflate(R.layout.preference_head, parent, false);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        Button btBack = view.findViewById(R.id.config_back);
        btBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (onBackButtonClickListener != null) {
                    onBackButtonClickListener.onClick(v);
                }
            }
        });
        View settingBarTheme=view.findViewById(R.id.setting_bar_theme);
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(context);
        settingBarTheme.setBackgroundColor(Color.parseColor(preferences.getString("theme_color","#474747")));
    }

    public void setOnBackButtonClickListener(View.OnClickListener onClickListener) {
        this.onBackButtonClickListener = onClickListener;
    }
}
