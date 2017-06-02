package com.adyen.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.CheckBox;


public class CheckoutCheckBox extends CheckBox {


    public CheckoutCheckBox(Context context) {
        super(context);
    }

    public CheckoutCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckoutCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CheckoutCheckBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void forceRippleAnimation() {
        if (Build.VERSION.SDK_INT >= 21) {
            final Drawable background = this.getBackground();
            if (background instanceof RippleDrawable) {
                background.setState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled});
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        background.setState(new int[]{});
                    }
                }, 200);
            }
        }
    }

}
