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

import com.adyen.checkout.base.component.validator.Validity;

public final class NumberValidatorImpl implements NumberValidator {
    private static final int RADIX = 10;
    private static final int FIVE_DIGIT = 5;
    private final char mNumberSeparator;

    NumberValidatorImpl(char numberSeparator) {
        mNumberSeparator = numberSeparator;
    }

    @NonNull
    @Override
    public NumberValidationResult validateNumber(@NonNull String number, boolean isOneClick) {

        if (isOneClick) {
            return new NumberValidationResult(Validity.VALID, number);
        }

        final String normalizedNumber = normalize(number, mNumberSeparator);
        final int length = normalizedNumber.length();

        if (!isDigitsAndSeparatorsOnly(normalizedNumber, mNumberSeparator)) {
            return new NumberValidationResult(Validity.INVALID, null);
        } else if (length > NUMBER_MAXIMUM_LENGTH) {
            return new NumberValidationResult(Validity.INVALID, null);
        } else if (length < NUMBER_MINIMUM_LENGTH) {
            return new NumberValidationResult(Validity.PARTIAL, normalizedNumber);
        } else if (isLuhnChecksumValid(normalizedNumber)) {
            return new NumberValidationResult(Validity.VALID, normalizedNumber);
        } else if (length == NUMBER_MAXIMUM_LENGTH) {
            return new NumberValidationResult(Validity.INVALID, null);
        } else {
            return new NumberValidationResult(Validity.PARTIAL, normalizedNumber);
        }
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private boolean isLuhnChecksumValid(@NonNull String normalizedNumber) {
        int s1 = 0;
        int s2 = 0;
        final String reverse = new StringBuffer(normalizedNumber).reverse().toString();

        for (int i = 0; i < reverse.length(); i++) {

            final int digit = Character.digit(reverse.charAt(i), RADIX);

            if (i % 2 == 0) {
                s1 += digit;
            } else {
                s2 += 2 * digit;

                if (digit >= FIVE_DIGIT) {
                    s2 -= 9;
                }
            }
        }

        return (s1 + s2) % 10 == 0;
    }
}
