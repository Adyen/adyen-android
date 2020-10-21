/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 28/8/2019.
 */

package com.adyen.checkout.sepa.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.util.AttributeSet;

import com.adyen.checkout.base.ui.view.AdyenTextInputEditText;
import com.adyen.checkout.sepa.Iban;

public class IbanInput extends AdyenTextInputEditText {

    public IbanInput(@NonNull Context context) {
        this(context, null);
    }

    public IbanInput(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IbanInput(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        enforceMaxInputLength(Iban.getFormattedMaxLength());
    }

    @Override
    protected void afterTextChanged(@NonNull Editable editable) {
        final String initial = editable.toString();
        final String processed = Iban.format(initial);

        if (!initial.equals(processed)) {
            editable.replace(0, initial.length(), processed);
        }

        super.afterTextChanged(editable);
    }
}
