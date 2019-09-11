/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 2/9/2019.
 */

package com.adyen.checkout.card.data;

import android.support.annotation.NonNull;

import com.adyen.checkout.base.component.OutputData;
import com.adyen.checkout.base.validation.ValidatedField;

public final class CardOutputData implements OutputData {

    private final ValidatedField<String> mHolderNameField;
    private final ValidatedField<String> mCardNumberField;
    private final ValidatedField<ExpiryDate> mExpiryDateField;
    private final ValidatedField<String> mSecurityCodeField;

    private boolean mIsStoredPaymentMethodEnable;

    private static final ValidatedField<String> EMPTY_HOLDER_NAME_FILED = new ValidatedField<>("", ValidatedField.Validation.PARTIAL);
    private static final ValidatedField<String> EMPTY_CARD_NUMBER_FILED = new ValidatedField<>("", ValidatedField.Validation.PARTIAL);
    private static final ValidatedField<ExpiryDate> EMPTY_EXPIRY_DATE_FILED =
            new ValidatedField<>(ExpiryDate.EMPTY_DATE, ValidatedField.Validation.PARTIAL);
    private static final ValidatedField<String> EMPTY_SECURITY_CODE_FILED = new ValidatedField<>("", ValidatedField.Validation.PARTIAL);

    /**
     * Constructs a {@link com.adyen.checkout.card.CardComponent} object.
     */
    public CardOutputData(
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

    /**
     * Constructs a {@link com.adyen.checkout.card.CardComponent} object.
     * With empty objects.
     */
    public CardOutputData() {
        mCardNumberField = EMPTY_CARD_NUMBER_FILED;
        mHolderNameField = EMPTY_HOLDER_NAME_FILED;
        mExpiryDateField = EMPTY_EXPIRY_DATE_FILED;
        mSecurityCodeField = EMPTY_SECURITY_CODE_FILED;
    }

    @NonNull
    public ValidatedField<String>  getCardNumberField() {
        return mCardNumberField;
    }

    @NonNull
    public ValidatedField<ExpiryDate> getExpiryDateField() {
        return mExpiryDateField;
    }

    @NonNull
    public ValidatedField<String>  getSecurityCodeField() {
        return mSecurityCodeField;
    }

    @NonNull
    public ValidatedField<String>  getHolderNameField() {
        return mHolderNameField;
    }

    /**
     * Check if object is created by default constructor.
     */
    public boolean isEmpty() {
        return mCardNumberField.equals(EMPTY_CARD_NUMBER_FILED)
                && mHolderNameField.equals(EMPTY_HOLDER_NAME_FILED)
                && mSecurityCodeField.equals(EMPTY_SECURITY_CODE_FILED)
                && mExpiryDateField.equals(EMPTY_EXPIRY_DATE_FILED);
    }

    @Override
    public boolean isValid() {
        if (!isEmpty()) {
            return mCardNumberField.isValid()
                    && mExpiryDateField.isValid()
                    && mSecurityCodeField.isValid()
                    && mHolderNameField.isValid();
        }

        return false;
    }

    public void setStoredPaymentMethodStatus(boolean storedPaymentMethodEnable) {
        mIsStoredPaymentMethodEnable = storedPaymentMethodEnable;
    }

    public boolean isStoredPaymentMethodEnable() {
        return mIsStoredPaymentMethodEnable;
    }
}
