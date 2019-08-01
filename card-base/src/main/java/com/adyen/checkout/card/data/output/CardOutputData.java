/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 19/3/2019.
 */

package com.adyen.checkout.card.data.output;

import android.support.annotation.NonNull;

import com.adyen.checkout.base.component.data.output.OutputData;

public final class CardOutputData implements OutputData {

    private final HolderNameField mHolderNameField;
    private final CardNumberField mCardNumberField;
    private final ExpiryDateField mExpiryDateField;
    private final SecurityCodeField mSecurityCodeField;
    private boolean mIsStoredPaymentMethodEnable;

    public static final HolderNameField EMPTY_HOLDER_NAME_FILED = new HolderNameField(null);
    public static final CardNumberField EMPTY_CARD_NUMBER_FILED = new CardNumberField(null);
    public static final ExpiryDateField EMPTY_EXPIRY_DATE_FILED = new ExpiryDateField(null);
    public static final SecurityCodeField EMPTY_SECURITY_CODE_FILED = new SecurityCodeField(null);

    /**
     * Constructs a {@link com.adyen.checkout.card.CardComponent} object.
     **/
    public CardOutputData(@NonNull CardNumberField cardNumberField, @NonNull ExpiryDateField expiryDateField,
            @NonNull SecurityCodeField securityCodeField, @NonNull HolderNameField holderNameField) {
        this(cardNumberField, expiryDateField, securityCodeField, holderNameField, false);
    }

    /**
     * Constructs a {@link com.adyen.checkout.card.CardComponent} object.
     */
    public CardOutputData(@NonNull CardNumberField cardNumberField, @NonNull ExpiryDateField expiryDateField,
            @NonNull SecurityCodeField securityCodeField, @NonNull HolderNameField holderNameField, boolean isStoredPaymentMethodEnable) {
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
    public CardNumberField getCardNumberField() {
        return mCardNumberField;
    }

    @NonNull
    public ExpiryDateField getExpiryDateField() {
        return mExpiryDateField;
    }

    @NonNull
    public SecurityCodeField getSecurityCodeField() {
        return mSecurityCodeField;
    }

    @NonNull
    public HolderNameField getHolderNameField() {
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
            return mCardNumberField.getValidationResult().isValid()
                    && mExpiryDateField.getValidationResult().isValid()
                    && mSecurityCodeField.getValidationResult().isValid()
                    && mHolderNameField.getValidationResult().isValid();
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
