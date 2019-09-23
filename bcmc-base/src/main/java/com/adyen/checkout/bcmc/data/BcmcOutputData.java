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

    private static final ValidatedField<String> EMPTY_CARD_NUMBER_FILED = new ValidatedField<>("", ValidatedField.Validation.PARTIAL);
    private static final ValidatedField<ExpiryDate> EMPTY_EXPIRY_DATE_FILED =
            new ValidatedField<>(ExpiryDate.EMPTY_DATE, ValidatedField.Validation.PARTIAL);

    public BcmcOutputData(
            @NonNull ValidatedField<String> cardNumberField,
            @NonNull ValidatedField<ExpiryDate> expiryDateField
    ) {
        mCardNumberField = cardNumberField;
        mExpiryDateField = expiryDateField;
    }

    public BcmcOutputData() {
        mCardNumberField = EMPTY_CARD_NUMBER_FILED;
        mExpiryDateField = EMPTY_EXPIRY_DATE_FILED;
    }

    @NonNull
    public ValidatedField<String> getCardNumberField() {
        return mCardNumberField;
    }

    @NonNull
    public ValidatedField<ExpiryDate> getExpiryDateField() {
        return mExpiryDateField;
    }

    /**
     * Check if object is created by default constructor.
     */
    public boolean isEmpty() {
        return mCardNumberField.equals(EMPTY_CARD_NUMBER_FILED)
                && mExpiryDateField.equals(EMPTY_EXPIRY_DATE_FILED);
    }

    @Override
    public boolean isValid() {
        if (!isEmpty()) {
            return mCardNumberField.isValid()
                    && mExpiryDateField.isValid();
        }

        return false;
    }
}
