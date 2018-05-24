package com.adyen.ui.views;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

public class CheckoutButton extends AppCompatButton {

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
