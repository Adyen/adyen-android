/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 19/3/2019.
 */

package com.adyen.checkout.card.data.output;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.component.data.output.OutputData;

public final class CardOutputData implements OutputData {

    private final NumberField mNumber;
    private final ExpiryDateField mExpiryDate;
    private final SecurityCodeField mSecurityCode;
    private final HolderNameField mHolderNameField;

    /**
     * Constructs a {@link com.adyen.checkout.card.CardComponent} object.
     *
     * @param number       {@link NumberField}
     * @param expiryDate   {@link ExpiryDateField}
     * @param securityCode {@link SecurityCodeField}
     */
    public CardOutputData(@NonNull NumberField number, @NonNull ExpiryDateField expiryDate, @Nullable SecurityCodeField securityCode) {
        this(number, expiryDate, securityCode, null);
    }

    /**
     * Constructs a {@link com.adyen.checkout.card.CardComponent} object.
     *
     * @param number          {@link NumberField}
     * @param expiryDate      {@link ExpiryDateField}
     * @param securityCode    {@link SecurityCodeField}
     * @param holderNameField {@link HolderNameField}
     */
    public CardOutputData(@NonNull NumberField number, @NonNull ExpiryDateField expiryDate, @Nullable SecurityCodeField securityCode,
            @Nullable HolderNameField holderNameField) {
        mNumber = number;
        mExpiryDate = expiryDate;
        mSecurityCode = securityCode;
        mHolderNameField = holderNameField;
    }

    public CardOutputData() {
        this(new NumberField(), new ExpiryDateField(), new SecurityCodeField(), new HolderNameField());
    }

    @NonNull
    public NumberField getNumber() {
        return mNumber;
    }

    @NonNull
    public ExpiryDateField getExpiryDate() {
        return mExpiryDate;
    }

    @NonNull
    public SecurityCodeField getSecurityCode() {
        return mSecurityCode;
    }

    @Nullable
    public HolderNameField getHolderNameField() {
        return mHolderNameField;
    }

    @Override
    public boolean isValid() {
        final boolean requiredFieldsValidation = mNumber.getValidationResult().isValid()
                && mExpiryDate.getValidationResult().isValid()
                && mSecurityCode.getValidationResult().isValid();

        return (mHolderNameField == null) ? requiredFieldsValidation
                : mHolderNameField.getValidationResult().isValid() && requiredFieldsValidation;
    }
}
