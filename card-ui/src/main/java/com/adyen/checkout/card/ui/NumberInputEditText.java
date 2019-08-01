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
import android.text.InputFilter;
import android.text.InputType;
import android.util.AttributeSet;

public class NumberInputEditText extends AdyenTextInputEditText {

    private static final int NO_LENGTH_DEFINED = -1;

    public NumberInputEditText(@NonNull Context context) {
        this(context, null);
    }

    public NumberInputEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor of NumberInputEditText.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public NumberInputEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (maxLength() != NO_LENGTH_DEFINED) {
            setInputType(InputType.TYPE_CLASS_PHONE);
            setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength())});
        }

        setInputType(InputType.TYPE_CLASS_PHONE);
    }

    public int maxLength() {
        return NO_LENGTH_DEFINED;
    }

}
