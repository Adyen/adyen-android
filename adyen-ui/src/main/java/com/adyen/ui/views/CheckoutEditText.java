package com.adyen.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EditText;


public class CheckoutEditText extends EditText {
    public CheckoutEditText(Context context) {
        super(context);
    }

    public CheckoutEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckoutEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CheckoutEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

}
