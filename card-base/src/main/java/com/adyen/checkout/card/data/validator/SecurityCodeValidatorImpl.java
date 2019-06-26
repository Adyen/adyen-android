/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 18/3/2019.
 */

package com.adyen.checkout.card.data.validator;

import static com.adyen.checkout.card.data.validator.ValidatorUtils.isDigitsAndSeparatorsOnly;
import static com.adyen.checkout.card.data.validator.ValidatorUtils.normalize;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.component.validator.Validity;
import com.adyen.checkout.card.model.CardType;

public final class SecurityCodeValidatorImpl implements SecurityCodeValidator {
    public static final int GENERAL_CARD_SECURITY_CODE_SIZE = 3;
    public static final int AMEX_SECURITY_CODE_SIZE = 4;

    @NonNull
    @Override
    public SecurityCodeValidationResult validateSecurityCode(@NonNull String securityCode, boolean isRequired, @Nullable CardType cardType) {
        final String normalizedSecurityCode = normalize(securityCode);
        final int length = normalizedSecurityCode.length();

        if (!isDigitsAndSeparatorsOnly(normalizedSecurityCode)) {
            return new SecurityCodeValidationResult(Validity.INVALID, null);
        } else if (length > SECURITY_CODE_MAXIMUM_LENGTH) {
            return new SecurityCodeValidationResult(Validity.INVALID, null);
        } else if (length < SECURITY_CODE_MINIMUM_LENGTH) {
            if (length == 0) {
                final Validity validity = isRequired ? Validity.INVALID : Validity.VALID;

                return new SecurityCodeValidationResult(validity, null);
            } else {
                return new SecurityCodeValidationResult(Validity.PARTIAL, normalizedSecurityCode);
            }
        } else {
            if (cardType == CardType.AMERICAN_EXPRESS) {
                return new SecurityCodeValidationResult(length == AMEX_SECURITY_CODE_SIZE ? Validity.VALID : Validity.PARTIAL,
                        normalizedSecurityCode);
            } else if (cardType != null) {
                if (length == GENERAL_CARD_SECURITY_CODE_SIZE) {
                    return new SecurityCodeValidationResult(Validity.VALID, normalizedSecurityCode);
                } else {
                    return new SecurityCodeValidationResult(Validity.INVALID, null);
                }
            } else {
                return new SecurityCodeValidationResult(Validity.VALID, normalizedSecurityCode);
            }
        }
    }
}
