/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/8/2020.
 */

package com.adyen.checkout.bcmc;

import androidx.annotation.NonNull;

import com.adyen.checkout.components.base.OutputData;
import com.adyen.checkout.components.ui.FieldState;
import com.adyen.checkout.card.data.ExpiryDate;

public final class BcmcOutputData implements OutputData {

    private final FieldState<String> mCardNumberField;
    private final FieldState<ExpiryDate> mExpiryDateField;

    BcmcOutputData(
            @NonNull FieldState<String> cardNumberField,
            @NonNull FieldState<ExpiryDate> expiryDateField
    ) {
        mCardNumberField = cardNumberField;
        mExpiryDateField = expiryDateField;
    }

    @NonNull
    public FieldState<String> getCardNumberField() {
        return mCardNumberField;
    }

    @NonNull
    public FieldState<ExpiryDate> getExpiryDateField() {
        return mExpiryDateField;
    }

    @Override
    public boolean isValid() {
        return mCardNumberField.getValidation().isValid()
                && mExpiryDateField.getValidation().isValid();
    }
}
