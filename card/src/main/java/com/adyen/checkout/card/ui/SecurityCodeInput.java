/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 19/7/2019.
 */

package com.adyen.checkout.card.ui;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SecurityCodeInput extends CardNumberInput {

    private static final int MAX_LENGTH = 4;

    public SecurityCodeInput(@NonNull Context context) {
        this(context, null);
    }

    public SecurityCodeInput(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SecurityCodeInput(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        enforceMaxInputLength(MAX_LENGTH);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setAutofillHints(AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE);
        }
    }
}
