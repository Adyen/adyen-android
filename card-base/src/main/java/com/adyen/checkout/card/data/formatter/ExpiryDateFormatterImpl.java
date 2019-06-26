/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 19/3/2019.
 */

package com.adyen.checkout.card.data.formatter;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class ExpiryDateFormatterImpl implements ExpiryDateFormatter {
    private static final int TWO_DIGIT = 2;
    private static final int ONE_DIGIT = 1;
    private final char mExpiryDateSeparatorChar;
    private final String mExpiryDateSeparatorString;

    private final String mExpiryDateFormat;

    ExpiryDateFormatterImpl(char expiryDateSeparator) {
        mExpiryDateSeparatorChar = expiryDateSeparator;
        mExpiryDateSeparatorString = String.valueOf(mExpiryDateSeparatorChar);
        mExpiryDateFormat = "MM" + expiryDateSeparator + "yy";
    }

    @NonNull
    @Override
    public String formatExpiryDate(int expiryMonth, int expiryYear) {
        final Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.MONTH, expiryMonth - 1);
        calendar.set(Calendar.YEAR, expiryYear);
        final Date date = calendar.getTime();

        return new SimpleDateFormat(mExpiryDateFormat, Locale.US).format(date);
    }

    @NonNull
    @Override
    public String formatExpiryDate(@NonNull String expiryDate, @NonNull String prevExpiryDate) {
        final StringBuilder sb = new StringBuilder(expiryDate);

        // user try to remove ExpiryDateSeparator, we don't need to format it since it's just two number!
        if (prevExpiryDate.length() > expiryDate.length() && prevExpiryDate.endsWith(mExpiryDateSeparatorString)) {
            return expiryDate;
        }

        if (expiryDate.length() == ONE_DIGIT && expiryDate.charAt(0) > '1') {
            sb.insert(0, "0");
        }

        if (expiryDate.length() == TWO_DIGIT) {
            if (expiryDate.matches("\\d\\" + mExpiryDateSeparatorChar)) {
                sb.insert(0, "0");
            } else if (!expiryDate.contains(mExpiryDateSeparatorString)) {
                sb.append(mExpiryDateSeparatorChar);
            }
        }

        for (int i = 0; i < expiryDate.length(); i++) {
            final char c = expiryDate.charAt(i);

            if (i == TWO_DIGIT) {
                if (c != mExpiryDateSeparatorChar) {
                    if (!Character.isDigit(c)) {
                        sb.replace(i, i + 1, mExpiryDateSeparatorString);
                    } else {
                        sb.insert(i, mExpiryDateSeparatorString);
                    }
                }
            } else {
                if (!Character.isDigit(c)) {
                    sb.delete(i, i + 1);
                }
            }
        }

        return sb.toString();
    }
}
