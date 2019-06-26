/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 15/3/2019.
 */

package com.adyen.checkout.card.data.validator;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.card.model.CardType;

public final class CardValidatorImpl implements CardValidator {
    private final HolderNameValidator mHolderNameValidator;
    private final NumberValidator mNumberValidator;
    private final ExpiryDateValidator mExpiryDateValidator;
    private final SecurityCodeValidator mSecurityCodeValidator;

    CardValidatorImpl(
            @NonNull HolderNameValidator holderNameValidator,
            @NonNull NumberValidator numberValidator,
            @NonNull ExpiryDateValidator expiryDateValidator,
            @NonNull SecurityCodeValidator securityCodeValidator
    ) {
        mHolderNameValidator = holderNameValidator;
        mNumberValidator = numberValidator;
        mExpiryDateValidator = expiryDateValidator;
        mSecurityCodeValidator = securityCodeValidator;
    }

    @NonNull
    @Override
    public HolderNameValidationResult validateHolderName(@NonNull String holderName, boolean isRequired) {
        return mHolderNameValidator.validateHolderName(holderName, isRequired);
    }

    @NonNull
    @Override
    public NumberValidationResult validateNumber(@NonNull String number) {
        return mNumberValidator.validateNumber(number);
    }

    @NonNull
    @Override
    public ExpiryDateValidationResult validateExpiryDate(@NonNull String expiryDate) {
        return mExpiryDateValidator.validateExpiryDate(expiryDate);
    }

    @NonNull
    @Override
    public SecurityCodeValidationResult validateSecurityCode(@NonNull String securityCode, boolean isRequired, @Nullable CardType cardType) {
        return mSecurityCodeValidator.validateSecurityCode(securityCode, isRequired, cardType);
    }
}
