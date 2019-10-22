/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */

package com.adyen.checkout.bcmc.data;

import android.support.annotation.NonNull;

import com.adyen.checkout.base.component.OutputData;
import com.adyen.checkout.base.validation.ValidatedField;
import com.adyen.checkout.card.data.ExpiryDate;

public final class BcmcOutputData implements OutputData {

    private final ValidatedField<String> mCardNumberField;
    private final ValidatedField<ExpiryDate> mExpiryDateField;

    public BcmcOutputData(
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
