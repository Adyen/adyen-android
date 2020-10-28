/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/8/2020.
 */

package com.adyen.checkout.card;

import androidx.annotation.NonNull;

import com.adyen.checkout.base.component.OutputData;
import com.adyen.checkout.base.validation.ValidatedField;
import com.adyen.checkout.card.data.ExpiryDate;

final class CardOutputData implements OutputData {

    private final ValidatedField<String> mHolderNameField;
    private final ValidatedField<String> mCardNumberField;
    private final ValidatedField<ExpiryDate> mExpiryDateField;
    private final ValidatedField<String> mSecurityCodeField;

    private boolean mIsStoredPaymentMethodEnable;

    /**
     * Constructs a {@link com.adyen.checkout.card.CardComponent} object.
     */
    CardOutputData(
            @NonNull ValidatedField<String> cardNumberField,
            @NonNull ValidatedField<ExpiryDate> expiryDateField,
            @NonNull ValidatedField<String> securityCodeField,
            @NonNull ValidatedField<String> holderNameField,
            boolean isStoredPaymentMethodEnable
    ) {
        mCardNumberField = cardNumberField;
        mExpiryDateField = expiryDateField;
        mSecurityCodeField = securityCodeField;
        mHolderNameField = holderNameField;
        mIsStoredPaymentMethodEnable = isStoredPaymentMethodEnable;
    }

    @NonNull
    public ValidatedField<String> getCardNumberField() {
        return mCardNumberField;
    }

    @NonNull
    public ValidatedField<ExpiryDate> getExpiryDateField() {
        return mExpiryDateField;
    }

    @NonNull
    public ValidatedField<String> getSecurityCodeField() {
        return mSecurityCodeField;
    }

    @NonNull
    public ValidatedField<String> getHolderNameField() {
        return mHolderNameField;
    }

    @Override
    public boolean isValid() {
        return mCardNumberField.isValid()
                && mExpiryDateField.isValid()
                && mSecurityCodeField.isValid()
                && mHolderNameField.isValid();
    }

    public void setStoredPaymentMethodStatus(boolean storedPaymentMethodEnable) {
        mIsStoredPaymentMethodEnable = storedPaymentMethodEnable;
    }

    public boolean isStoredPaymentMethodEnable() {
        return mIsStoredPaymentMethodEnable;
    }
}
