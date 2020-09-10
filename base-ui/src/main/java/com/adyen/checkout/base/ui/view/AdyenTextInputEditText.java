/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/8/2019.
 */

package com.adyen.checkout.base.ui.view;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;

public class AdyenTextInputEditText extends TextInputEditText {

    protected static final int NO_MAX_LENGTH = -1;

    @Nullable
    private Listener mListener;

    public AdyenTextInputEditText(@NonNull Context context) {
        this(context, null);
    }

    public AdyenTextInputEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor of AdyenTextInputEditText.
     */
    public AdyenTextInputEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr == 0 ? com.google.android.material.R.attr.editTextStyle : defStyleAttr);
        addTextChangedListener(getTextWatcher());
    }

    public void setOnChangeListener(@NonNull Listener listener) {
        this.mListener = listener;
    }

    @NonNull
    public String getRawValue() {
        return getText() != null ? getText().toString() : "";
    }

    @CallSuper
    protected void afterTextChanged(@NonNull Editable editable) {
        if (mListener != null) {
            mListener.onTextChanged(editable);
        }
    }

    protected void enforceMaxInputLength(int maxLength) {
        if (maxLength != NO_MAX_LENGTH) {
            setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        }
    }

    @NonNull
    private TextWatcher getTextWatcher() {
        return new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Empty
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Empty
            }

            @Override
            public void afterTextChanged(Editable s) {
                AdyenTextInputEditText.this.afterTextChanged(s);
            }
        };
    }

    public interface Listener {
        void onTextChanged(@NonNull Editable editable);
    }
}
