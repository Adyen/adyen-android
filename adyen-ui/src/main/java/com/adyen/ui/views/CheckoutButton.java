package com.adyen.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Button;

public class CheckoutButton extends Button {

    public static final int FULLY_OPAQUE = 255;
    public static final int TRANSPARENT = 96;

    public CheckoutButton(Context context) {
        super(context);
    }

    public CheckoutButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckoutButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CheckoutButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init() {

    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            getBackground().setAlpha(FULLY_OPAQUE);
        } else {
            getBackground().setAlpha(TRANSPARENT);
        }
    }
}
