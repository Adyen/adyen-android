package com.adyen.checkout.core.card.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.adyen.checkout.core.card.CardType;
import com.adyen.checkout.core.card.CardValidator;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 06/02/2018.
 */
public final class CardValidatorImpl implements CardValidator {
    @VisibleForTesting
    public static final int MAXIMUM_EXPIRED_MONTHS = 3;

    @VisibleForTesting
    public static final int MAXIMUM_YEARS_IN_FUTURE = 20;

    @VisibleForTesting
    public static final int MONTHS_IN_YEAR = 12;

    private final char mNumberSeparator;

    private final char mExpiryDateSeparator;

    private final Pattern mExpiryDatePattern;

    public CardValidatorImpl(char numberSeparator, char expiryDateSeparator) {
        mNumberSeparator = numberSeparator;
        mExpiryDateSeparator = expiryDateSeparator;
        mExpiryDatePattern = Pattern.compile("(0?[1-9]|1[0-2])\\" + expiryDateSeparator + "((20)?\\d{2})");
    }

    @NonNull
    @Override
    public HolderNameValidationResult validateHolderName(@NonNull String holderName, boolean isRequired) {
        String normalizedHolderName = holderName.trim();

        if (normalizedHolderName.isEmpty()) {
            Validity validity = isRequired ? Validity.INVALID : Validity.VALID;

            return new HolderNameValidationResult(validity, null);
        } else {
            return new HolderNameValidationResult(Validity.VALID, normalizedHolderName);
        }
    }

    @NonNull
    @Override
    public NumberValidationResult validateNumber(@NonNull String number) {
        String normalizedNumber = normalize(number, mNumberSeparator);
        int length = normalizedNumber.length();

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

    @NonNull
    @Override
    public ExpiryDateValidationResult validateExpiryDate(@NonNull String expiryDate) {
        String normalizedExpiryDate = normalize(expiryDate);
        Matcher matcher = mExpiryDatePattern.matcher(normalizedExpiryDate);
        String[] parts = expiryDate.split("\\" + String.valueOf(mExpiryDateSeparator));

        if (matcher.matches()) {
            Integer expiryMonth = Integer.parseInt(parts[0]);
            Integer expiryYear = makeFourDigitYear(parts[1]);
            Validity validity = isAcceptedForTransaction(expiryMonth, expiryYear) ? Validity.VALID : Validity.INVALID;

            return new ExpiryDateValidationResult(validity, expiryMonth, expiryYear);
        } else if (matcher.hitEnd()) {
            return new ExpiryDateValidationResult(Validity.PARTIAL, parts.length == 2 ? Integer.parseInt(parts[0]) : null, null);
        } else {
            return new ExpiryDateValidationResult(Validity.INVALID, null, null);
        }
    }

    @NonNull
    @Override
    public SecurityCodeValidationResult validateSecurityCode(@NonNull String securityCode, boolean isRequired, @Nullable CardType cardType) {
        String normalizedSecurityCode = normalize(securityCode);
        int length = normalizedSecurityCode.length();

        if (!isDigitsAndSeparatorsOnly(normalizedSecurityCode)) {
            return new SecurityCodeValidationResult(Validity.INVALID, null);
        } else if (length > SECURITY_CODE_MAXIMUM_LENGTH) {
            return new SecurityCodeValidationResult(Validity.INVALID, null);
        } else if (length < SECURITY_CODE_MINIMUM_LENGTH) {
            if (length == 0) {
                Validity validity = isRequired ? Validity.INVALID : Validity.VALID;

                return new SecurityCodeValidationResult(validity, null);
            } else {
                return new SecurityCodeValidationResult(Validity.PARTIAL, normalizedSecurityCode);
            }
        } else {
            if (cardType == CardType.AMERICAN_EXPRESS) {
                return new SecurityCodeValidationResult(length == 4 ? Validity.VALID : Validity.PARTIAL, normalizedSecurityCode);
            } else if (cardType != null) {
                if (length == 3) {
                    return new SecurityCodeValidationResult(Validity.VALID, normalizedSecurityCode);
                } else {
                    return new SecurityCodeValidationResult(Validity.INVALID, null);
                }
            } else {
                return new SecurityCodeValidationResult(Validity.VALID, normalizedSecurityCode);
            }
        }
    }

    @NonNull
    private String normalize(@NonNull String value, @NonNull char... additionalCharsToReplace) {
        return value.replaceAll("[\\s" + new String(additionalCharsToReplace) + "]", "");
    }

    private boolean isDigitsAndSeparatorsOnly(@NonNull String value, @NonNull char... separators) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            for (char separator : separators) {
                if (!Character.isDigit(c) && separator != c) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isLuhnChecksumValid(@NonNull String normalizedNumber) {
        int s1 = 0, s2 = 0;
        String reverse = new StringBuffer(normalizedNumber).reverse().toString();

        for (int i = 0; i < reverse.length(); i++) {
            int digit = Character.digit(reverse.charAt(i), 10);

            if (i % 2 == 0) {
                s1 += digit;
            } else {
                s2 += 2 * digit;

                if (digit >= 5) {
                    s2 -= 9;
                }
            }
        }

        return (s1 + s2) % 10 == 0;
    }

    private int makeFourDigitYear(@NonNull String value) {
        switch (value.length()) {
            case 2:
                return Integer.parseInt("20" + value);
            case 4:
                return Integer.parseInt(value);
            default:
                throw new IllegalStateException("Year has invalid length.");
        }
    }

    private boolean isAcceptedForTransaction(int expiryMonth, int expiryYear) {
        Calendar calendar = GregorianCalendar.getInstance();
        // Calendar.MONTH is zero-based.
        int currentDateInMonths = calendar.get(Calendar.YEAR) * MONTHS_IN_YEAR + calendar.get(Calendar.MONTH) + 1;
        int expiryDateInMonths = expiryYear * MONTHS_IN_YEAR + expiryMonth;
        int limitDateInMonths = currentDateInMonths + MAXIMUM_YEARS_IN_FUTURE * MONTHS_IN_YEAR;

        return expiryDateInMonths > currentDateInMonths - MAXIMUM_EXPIRED_MONTHS && expiryDateInMonths <= limitDateInMonths;
    }
}
