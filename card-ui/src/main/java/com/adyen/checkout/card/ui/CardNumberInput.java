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

import com.adyen.checkout.base.ui.view.AdyenTextInputEditText;
import com.adyen.checkout.card.CardValidationUtils;

public class CardNumberInput extends AdyenTextInputEditText {

    private static final int MAX_DIGIT_SEPARATOR_COUNT = 4;
    private static final char DIGIT_SEPARATOR = ' ';

    public CardNumberInput(@NonNull Context context) {
        this(context, null);
    }

    public CardNumberInput(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardNumberInput(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        enforceMaxInputLength(CardValidationUtils.MAXIMUM_CARD_NUMBER_LENGTH  + MAX_DIGIT_SEPARATOR_COUNT);
    }

    @NonNull
    @Override
    public String getRawValue() {
        return getText().toString().replace(DIGIT_SEPARATOR + "", "");
    }

    @Override
    protected void afterTextChanged(@NonNull Editable editable) {
        final String initial = editable.toString();
        String processed = initial.trim();
        processed = processed.replaceAll("(\\d{4})(?=\\d)", "$1" + DIGIT_SEPARATOR);
        if (!initial.equals(processed)) {
            editable.replace(0, initial.length(), processed);
        }

        super.afterTextChanged(editable);
    }
}
