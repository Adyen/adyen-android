/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 19/7/2019.
 */

package com.adyen.checkout.card.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.util.AttributeSet;

public class ExpiryDateInput extends NumberInputEditText {

    private static final int MAX_LENGTH = 5;
    private static final char DATE_SEPARATOR = '/';
    private static final int ONE_DIGIT_MONTH = 1;

    public ExpiryDateInput(@NonNull Context context) {
        this(context, null);
    }

    public ExpiryDateInput(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpiryDateInput(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int maxLength() {
        return MAX_LENGTH;
    }

    @Override
    public void afterTextChanged(@NonNull Editable editable) {
        final String initial = editable.toString();
        String processed = initial.replaceAll("\\D", "");
        processed = processed.replaceAll("(\\d{2})(?=\\d)", "$1" + DATE_SEPARATOR);

        if (processed.length() == 1 && isStringInt(processed) && Integer.parseInt(processed) > ONE_DIGIT_MONTH) {
            processed = "0" + processed;
        }

        if (!initial.equals(processed)) {
            editable.replace(0, initial.length(), processed);
        }

        super.afterTextChanged(editable);
    }

    @NonNull
    @Override
    public String getRawValue() {
        return getText().toString().replace(DATE_SEPARATOR + "", "");
    }

    private boolean isStringInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
