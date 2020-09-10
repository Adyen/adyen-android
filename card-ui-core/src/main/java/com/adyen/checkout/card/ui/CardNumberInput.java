/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 19/7/2019.
 */

package com.adyen.checkout.card.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;

import com.adyen.checkout.base.ui.view.AdyenTextInputEditText;
import com.adyen.checkout.card.CardValidationUtils;

import java.util.Arrays;

public class CardNumberInput extends AdyenTextInputEditText {

    private static final int MAX_DIGIT_SEPARATOR_COUNT = 4;
    private static final char DIGIT_SEPARATOR = ' ';
    private static final String SUPPORTED_DIGITS = "0123456789";
    private static final int[] AMEX_CARD_NUMBER_MASK = {4, 6, 5, 4};
    private static final int[] DEFAULT_CARD_NUMBER_MASK = {4, 4, 4, 4, 3};
    private static final int START_OF_STRING = 0;

    private boolean mIsAmexCard;

    public CardNumberInput(@NonNull Context context) {
        this(context, null);
    }

    public CardNumberInput(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Input that support formatting for card number.
     */
    public CardNumberInput(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        enforceMaxInputLength(CardValidationUtils.MAXIMUM_CARD_NUMBER_LENGTH + MAX_DIGIT_SEPARATOR_COUNT);
        setInputType(InputType.TYPE_CLASS_NUMBER);
        setKeyListener(DigitsKeyListener.getInstance(SUPPORTED_DIGITS + DIGIT_SEPARATOR));
    }

    @NonNull
    @Override
    public String getRawValue() {
        return getText().toString().replace(String.valueOf(DIGIT_SEPARATOR), "");
    }

    /**
     * Enable Amex formatting.
     */
    public void setAmexCardFormat(boolean value) {

        // first time detecting is Amex card
        if (!mIsAmexCard && value) {
            this.mIsAmexCard = true;
            afterTextChanged(getEditableText());
            return;
        }

        this.mIsAmexCard = value;
    }

    @Override
    protected void afterTextChanged(@NonNull Editable editable) {
        final String initial = editable.toString();
        String processed = initial.trim().replaceAll(String.valueOf(DIGIT_SEPARATOR), "");
        processed = formatProcessedString(processed);
        if (!initial.equals(processed)) {
            editable.replace(0, initial.length(), processed);
        }

        super.afterTextChanged(editable);
    }

    @NonNull
    private String formatProcessedString(@NonNull String processedValue) {
        final String[] result = splitStringWithMask(processedValue, mIsAmexCard ? AMEX_CARD_NUMBER_MASK : DEFAULT_CARD_NUMBER_MASK);
        return TextUtils.join(DIGIT_SEPARATOR + "", result).trim();
    }

    @NonNull
    private String[] splitStringWithMask(@NonNull String value, @NonNull int... mask) {
        final String[] result = new String[mask.length];
        Arrays.fill(result, "");

        String tempValue = value;

        for (int indexOfMask = 0; indexOfMask < mask.length; indexOfMask++) {
            if (tempValue.length() >= mask[indexOfMask]) {
                result[indexOfMask] = tempValue.substring(START_OF_STRING, mask[indexOfMask]);
                tempValue = tempValue.substring(mask[indexOfMask]);
            } else {
                result[indexOfMask] = tempValue;
                break;
            }
        }

        return result;
    }
}
