package com.zbm.dainty.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;

/**
 * created by @author 张倍铭 on 2018/12/18 10:27
 */
public class PopupAgent {
    private View popupWindow;
    private View popupContent;
    private boolean isShowing;
    private ViewGroup decorView;
    private Animation inAnim, outAnim;
    private int[] margins;
    private OnDismissListener onDismissListener;

    private PopupAgent(Builder builder) {
        Context context = builder.context;
        this.popupWindow = builder.popupWindow;
        this.popupContent = builder.popupContent;
        View popupDark = builder.popupDark;
        this.inAnim = builder.inAnim;
        this.outAnim = builder.outAnim;
        this.margins = builder.margins;

        if (!(context instanceof Activity)) {
            throw new IllegalArgumentException("The context is not instanceof Activity!");
        }

        decorView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        if (popupDark != null) {
            popupDark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }

        if (popupContent != null) {
            popupContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void show() {
        if (isShowing){
            return;
        }
        popupContent.clearAnimation();
        decorView.removeView(popupWindow);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(margins[0], margins[1], margins[2], margins[3]);
        decorView.addView(popupWindow, params);
        popupContent.startAnimation(inAnim);
        isShowing = true;
    }

    public void dismiss() {
        if (!isShowing){
            return;
        }
        isShowing = false;
        outAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                decorView.removeView(popupWindow);
                if (onDismissListener != null) {
                    onDismissListener.onDismiss();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        popupContent.startAnimation(outAnim);

    }

    public static final class Builder {
        private Context context;
        private View popupWindow, popupContent, popupDark;
        private Animation inAnim, outAnim;
        private int[] margins = new int[4];

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setPopupWindow(View popupWindow) {
            this.popupWindow = popupWindow;
            return this;
        }

        public Builder setPopupContent(View popupContent) {
            this.popupContent = popupContent;
            return this;
        }

        public Builder setPopupDark(View popupDark) {
            this.popupDark = popupDark;
            return this;
        }

        public Builder setMargins(int left, int top, int right, int bottom) {
            margins[0] = left;
            margins[1] = top;
            margins[2] = right;
            margins[3] = bottom;
            return this;
        }

        public Builder setInAnimation(Animation inAnim) {
            this.inAnim = inAnim;
            return this;
        }

        public Builder setOutAnimation(Animation outAnim) {
            this.outAnim = outAnim;
            return this;
        }

        public PopupAgent build() {
            return new PopupAgent(this);
        }
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    public interface OnDismissListener {
        /**
         * 对话框消失
         */
        void onDismiss();
    }
}
