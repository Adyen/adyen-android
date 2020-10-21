/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/8/2020.
 */

package com.adyen.checkout.bcmc;

import androidx.annotation.NonNull;

import com.adyen.checkout.base.component.OutputData;
import com.adyen.checkout.base.validation.ValidatedField;
import com.adyen.checkout.card.data.ExpiryDate;

final class BcmcOutputData implements OutputData {

    private final ValidatedField<String> mCardNumberField;
    private final ValidatedField<ExpiryDate> mExpiryDateField;

    BcmcOutputData(
            @NonNull ValidatedField<String> cardNumberField,
            @NonNull ValidatedField<ExpiryDate> expiryDateField
    ) {
        mCardNumberField = cardNumberField;
        mExpiryDateField = expiryDateField;
    }

    @NonNull
    public ValidatedField<String> getCardNumberField() {
        return mCardNumberField;
    }

    @NonNull
    public ValidatedField<ExpiryDate> getExpiryDateField() {
        return mExpiryDateField;
    }

    @Override
    public boolean isValid() {
        return mCardNumberField.isValid()
                && mExpiryDateField.isValid();
    }
}
