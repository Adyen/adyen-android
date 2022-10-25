/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/8/2020.
 */

package com.adyen.checkout.bcmc;

import androidx.annotation.NonNull;

import com.adyen.checkout.card.data.ExpiryDate;
import com.adyen.checkout.components.base.OutputData;
import com.adyen.checkout.components.ui.FieldState;

public final class BcmcOutputData implements OutputData {

    private final FieldState<String> mCardNumberField;
    private final FieldState<ExpiryDate> mExpiryDateField;
    private final FieldState<String> mCardHolderNameField;
    private final boolean mIsStoredPaymentMethodEnabled;

    BcmcOutputData(
            @NonNull FieldState<String> cardNumberField,
            @NonNull FieldState<ExpiryDate> expiryDateField,
            @NonNull FieldState<String> cardHolderNameField,
            boolean isStoredPaymentMethodEnabled
    ) {
        mCardNumberField = cardNumberField;
        mExpiryDateField = expiryDateField;
        mCardHolderNameField = cardHolderNameField;
        mIsStoredPaymentMethodEnabled = isStoredPaymentMethodEnabled;
    }

    @NonNull
    public FieldState<String> getCardNumberField() {
        return mCardNumberField;
    }

    @NonNull
    public FieldState<ExpiryDate> getExpiryDateField() {
        return mExpiryDateField;
    }

    @NonNull
    public FieldState<String> getCardHolderNameField() {
        return mCardHolderNameField;
    }

    public boolean isStoredPaymentMethodEnabled() {
        return mIsStoredPaymentMethodEnabled;
    }

    @Override
    public boolean isValid() {
        return mCardNumberField.getValidation().isValid()
                && mExpiryDateField.getValidation().isValid()
                && mCardHolderNameField.getValidation().isValid();
    }
}
