/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */

package com.adyen.checkout.card;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.adyen.checkout.base.validation.ValidatedField;
import com.adyen.checkout.card.data.CardType;
import com.adyen.checkout.card.data.ExpiryDate;
import com.adyen.checkout.core.exception.NoConstructorException;
import com.adyen.checkout.core.util.StringUtil;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

public final class CardValidationUtils {

    private static final String PUBLIC_KEY_PATTERN = "([0-9]){5}\\|([A-Z]|[0-9]){512}";

    // Luhn Check
    private static final int RADIX = 10;
    private static final int FIVE_DIGIT = 5;
    // Card Number
    private static final int MINIMUM_CARD_NUMBER_LENGTH = 8;
    public static final int MAXIMUM_CARD_NUMBER_LENGTH = 19;
    public static final int GENERAL_CARD_NUMBER_LENGTH = 16;
    public static final int AMEX_CARD_NUMBER_LENGTH = 15;
    // Security Code
    private static final int GENERAL_CARD_SECURITY_CODE_SIZE = 3;
    private static final int AMEX_SECURITY_CODE_SIZE = 4;
    // Date
    private static final int MONTHS_IN_YEAR = 12;
    private static final int MAXIMUM_YEARS_IN_FUTURE = 30;
    private static final int MAXIMUM_EXPIRED_MONTHS = 3;

    public static boolean isPublicKeyValid(@Nullable String publicKey) {
        final Pattern pubKeyPattern = Pattern.compile(PUBLIC_KEY_PATTERN);
        return !TextUtils.isEmpty(publicKey) && pubKeyPattern.matcher(publicKey).find();
    }

    /**
     * Validate card number.
     */
    @NonNull
    public static ValidatedField<String> validateCardNumber(@NonNull String number) {

        final String normalizedNumber = StringUtil.normalize(number);
        final int length = normalizedNumber.length();

        final ValidatedField.Validation validation;

        if (!StringUtil.isDigitsAndSeparatorsOnly(normalizedNumber)) {
            validation = ValidatedField.Validation.INVALID;
        } else if (length > MAXIMUM_CARD_NUMBER_LENGTH) {
            validation = ValidatedField.Validation.INVALID;
        } else if (length < MINIMUM_CARD_NUMBER_LENGTH) {
            validation = ValidatedField.Validation.PARTIAL;
        } else if (isLuhnChecksumValid(normalizedNumber)) {
            validation = ValidatedField.Validation.VALID;
        } else if (length == MAXIMUM_CARD_NUMBER_LENGTH) {
            validation = ValidatedField.Validation.INVALID;
        } else {
            validation = ValidatedField.Validation.PARTIAL;
        }

        return new ValidatedField<>(number, validation);
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private static boolean isLuhnChecksumValid(@NonNull String normalizedNumber) {
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

    /**
     * Validate Expiry Date.
     */
    @NonNull
    public static ValidatedField<ExpiryDate> validateExpiryDate(@NonNull ExpiryDate expiryDate) {

        if (dateExists(expiryDate)) {
            final Calendar expiryDateCalendar = getExpiryCalendar(expiryDate);

            final Calendar maxFutureCalendar = GregorianCalendar.getInstance();
            maxFutureCalendar.add(Calendar.YEAR, MAXIMUM_YEARS_IN_FUTURE);

            final Calendar maxPastCalendar = GregorianCalendar.getInstance();
            maxPastCalendar.add(Calendar.MONTH, -MAXIMUM_EXPIRED_MONTHS);

            // higher than maxPast and lower than maxFuture
            if (expiryDateCalendar.compareTo(maxPastCalendar) >= 0 && expiryDateCalendar.compareTo(maxFutureCalendar) <= 0) {
                return new ValidatedField<>(expiryDate, ValidatedField.Validation.VALID);
            }
        }

        return new ValidatedField<>(expiryDate, ValidatedField.Validation.INVALID);
    }

    /**
     * Validate Security Code.
     * We always pass CardType null, but we can enforce size validation for Amex or otherwise if necessary.
     */
    @SuppressWarnings("SameParameterValue")
    @NonNull
    public static ValidatedField<String> validateSecurityCode(@NonNull String securityCode, @Nullable CardType cardType) {
        final String normalizedSecurityCode = StringUtil.normalize(securityCode);
        final int length = normalizedSecurityCode.length();

        ValidatedField.Validation validation = ValidatedField.Validation.INVALID;

        if (StringUtil.isDigitsAndSeparatorsOnly(normalizedSecurityCode)) {
            if (cardType == CardType.AMERICAN_EXPRESS && length == AMEX_SECURITY_CODE_SIZE) {
                validation = ValidatedField.Validation.VALID;
            } else if (length == GENERAL_CARD_SECURITY_CODE_SIZE && cardType != CardType.AMERICAN_EXPRESS) {
                validation = ValidatedField.Validation.VALID;
            }
        }

        return new ValidatedField<>(normalizedSecurityCode, validation);
    }

    private static boolean dateExists(@NonNull ExpiryDate expiryDate) {
        return expiryDate != ExpiryDate.EMPTY_DATE
                && expiryDate.getExpiryMonth() != ExpiryDate.EMPTY_VALUE
                && expiryDate.getExpiryYear() != ExpiryDate.EMPTY_VALUE
                && isValidMonth(expiryDate.getExpiryMonth())
                && expiryDate.getExpiryYear() > 0;
    }

    private static boolean isValidMonth(int month) {
        return month > 0 && month <= MONTHS_IN_YEAR;
    }

    private static Calendar getExpiryCalendar(@NonNull ExpiryDate expiryDate) {

        final Calendar expiryCalendar = GregorianCalendar.getInstance();
        expiryCalendar.clear();
        // First day of the expiry month. Calendar.MONTH is zero-based.
        expiryCalendar.set(expiryDate.getExpiryYear(), expiryDate.getExpiryMonth() - 1, 1);
        // Go to next month and remove 1 day to be on the last day of the expiry month.
        expiryCalendar.add(Calendar.MONTH, 1);
        expiryCalendar.add(Calendar.DAY_OF_MONTH, -1);

        return expiryCalendar;
    }

    private CardValidationUtils() {
        throw new NoConstructorException();
    }
}
