package com.adyen.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import com.adyen.ui.utils.AdyenInputValidator;

public class CardHolderEditText extends CheckoutEditText {

    private AdyenInputValidator validator;

    public CardHolderEditText(Context context) {
        super(context);
        init();
    }

    public CardHolderEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CardHolderEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CardHolderEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (validator != null) {
                    validator.setReady(CardHolderEditText.this, !s.toString().isEmpty());
                }
            }
        });
    }

    public void setValidator(AdyenInputValidator validator) {
        this.validator = validator;
        this.validator.addInputField(CardHolderEditText.this);
    }
}
