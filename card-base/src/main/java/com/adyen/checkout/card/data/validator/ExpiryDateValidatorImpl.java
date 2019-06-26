/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 18/3/2019.
 */

package com.adyen.checkout.card.data.validator;

import static com.adyen.checkout.card.data.validator.ValidatorUtils.normalize;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.adyen.checkout.base.component.validator.Validity;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ExpiryDateValidatorImpl implements ExpiryDateValidator {
    @VisibleForTesting
    public static final int MAXIMUM_EXPIRED_MONTHS = 3;

    @VisibleForTesting
    public static final int MAXIMUM_YEARS_IN_FUTURE = 20;

    @VisibleForTesting
    public static final int MONTHS_IN_YEAR = 12;

    private final char mExpiryDateSeparator;

    private final Pattern mExpiryDatePattern;

    ExpiryDateValidatorImpl(char expiryDateSeparator) {
        mExpiryDateSeparator = expiryDateSeparator;
        mExpiryDatePattern = Pattern.compile("(0?[1-9]|1[0-2])\\" + expiryDateSeparator + "((20)?\\d{2})");
    }

    @NonNull
    @Override
    public ExpiryDateValidationResult validateExpiryDate(@NonNull String expiryDate) {
        final String normalizedExpiryDate = normalize(expiryDate);
        final Matcher matcher = mExpiryDatePattern.matcher(normalizedExpiryDate);
        final String[] parts = expiryDate.split("\\" + mExpiryDateSeparator);

        if (matcher.matches()) {
            final Integer expiryMonth = Integer.parseInt(parts[0]);
            final Integer expiryYear = makeFourDigitYear(parts[1]);
            final Validity validity = isAcceptedForTransaction(expiryMonth, expiryYear) ? Validity.VALID : Validity.INVALID;

            return new ExpiryDateValidationResult(validity, expiryMonth, expiryYear);
        } else if (matcher.hitEnd()) {
            return new ExpiryDateValidationResult(Validity.PARTIAL, parts.length == 2 ? Integer.parseInt(parts[0]) : null, null);
        } else {
            return new ExpiryDateValidationResult(Validity.INVALID, null, null);
        }
    }

    @SuppressWarnings("checkstyle:MagicNumber")
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
        final Calendar calendar = GregorianCalendar.getInstance();
        // Calendar.MONTH is zero-based.
        final int currentDateInMonths = calendar.get(Calendar.YEAR) * MONTHS_IN_YEAR + calendar.get(Calendar.MONTH) + 1;
        final int expiryDateInMonths = expiryYear * MONTHS_IN_YEAR + expiryMonth;
        final int limitDateInMonths = currentDateInMonths + MAXIMUM_YEARS_IN_FUTURE * MONTHS_IN_YEAR;

        return expiryDateInMonths > currentDateInMonths - MAXIMUM_EXPIRED_MONTHS && expiryDateInMonths <= limitDateInMonths;
    }
}
