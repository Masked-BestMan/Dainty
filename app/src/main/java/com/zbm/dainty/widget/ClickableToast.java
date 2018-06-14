package com.zbm.dainty.widget;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zbm.dainty.R;

/**
 * Created by zbm阿铭 on 2018/2/19.
 */

public class ClickableToast extends Toast {
    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context The context to use.  Usually your {@link Application}
     *                or {@link Activity} object.
     */
    public ClickableToast(Context context) {
        super(context);
    }

    public static Toast makeClickText(@NonNull Context context, @NonNull CharSequence text, CharSequence title, int duration, final OnToastClickListener onToastClickListener) {
        Toast result = new Toast(context);
        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.clickable_toast, null);

        TextView tv = v.findViewById(R.id.title_toast);
        tv.setText(text);
        TextView b = v.findViewById(R.id.button_toast);
        b.setText(title);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToastClickListener.onToastClick();
            }
        });

        result.setView(v);
        result.setDuration(duration);

        return result;
    }

    public interface OnToastClickListener {
        void onToastClick();
    }
}
