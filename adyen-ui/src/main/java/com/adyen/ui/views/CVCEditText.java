package com.adyen.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;

import com.adyen.ui.R;
import com.adyen.ui.utils.AdyenInputValidator;

import java.util.ArrayList;


public class CVCEditText extends CheckoutEditText {

    public static final int CVC_MAX_LENGTH_AMEX = 4;
    public static final int CVC_MAX_LENGTH = 3;

    private int maxLength = CVC_MAX_LENGTH;

    private AdyenInputValidator validator;

    private void init() {
        ArrayList<InputFilter> cvcFilters = new ArrayList<>();
        cvcFilters.add(new InputFilter.LengthFilter(CVC_MAX_LENGTH));
        cvcFilters.add(new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (!source.toString().matches(".*\\d.*")) {
                    return "";
                } else {
                    return source;
                }
            }
        });
        this.setFilters(cvcFilters.toArray(new InputFilter[cvcFilters.size()]));
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
                    validator.setReady(CVCEditText.this, hasValidInput());
                }

                if (hasValidInput()) {
                    View next = focusSearch(View.FOCUS_DOWN);
                    if (next != null) {
                        next.requestFocus();
                    }
                }
            }
        });

        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    CVCEditText.this.setTextColor(ContextCompat.getColor(getContext(), R.color.black_text));
                } else {
                    if (!hasValidInput()) {
                        CVCEditText.this.setTextColor(ContextCompat.getColor(getContext(),
                                R.color.red_invalid_input_highlight));
                    }
                }
            }
        });
    }

    public CVCEditText(Context context) {
        super(context);
        init();
    }

    public CVCEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CVCEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CVCEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setValidator(AdyenInputValidator validator) {
        this.validator = validator;
        this.validator.addInputField(CVCEditText.this);
    }

    public String getCVC() {
        return this.getText().toString();
    }

    public void setMaxLength(final int newMaxLength) {
        this.maxLength = newMaxLength;
        InputFilter[] inputFilters = getFilters();
        for (int i = 0; i < inputFilters.length; i++) {
            if (inputFilters[i] instanceof InputFilter.LengthFilter) {
                inputFilters[i] = new InputFilter.LengthFilter(maxLength);
                break;
            }
        }
        this.setFilters(inputFilters);
        if (this.getText().toString().length() > maxLength) {
            this.setText(this.getText().toString().substring(0, maxLength));
        }
    }

    public boolean hasValidInput() {
        return this.getText().toString().length() == maxLength;
    }

}
