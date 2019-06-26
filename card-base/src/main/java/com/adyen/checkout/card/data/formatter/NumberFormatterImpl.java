/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 19/3/2019.
 */

package com.adyen.checkout.card.data.formatter;

import android.support.annotation.NonNull;

@SuppressWarnings("AvoidEscapedUnicodeCharacters")
public final class NumberFormatterImpl implements NumberFormatter {
    private static final String NUMBER_FORMAT_REGEX = "(.{4})";

    private static final int VISIBLE_CHARS_MASK = 4;
    private static final char MASKING_CHAR = '\u2022';
    private static final char NON_BREAKING_SPACE = '\u00A0';

    private static final int MAX_LENGTH = 19;

    private final char mNumberSeparatorChar;
    private final String mNumberSeparatorString;

    NumberFormatterImpl(char numberSeparator) {
        mNumberSeparatorChar = numberSeparator;
        mNumberSeparatorString = String.valueOf(mNumberSeparatorChar);
    }

    @NonNull
    @Override
    public String unformatNumber(@NonNull String formattedNumber) {
        return formattedNumber.replaceAll(mNumberSeparatorString, "");
    }

    @NonNull
    @Override
    public String formatNumber(@NonNull String number) {

        String trimmedNumber = number;

        if (number.length() > MAX_LENGTH) {
            trimmedNumber = number.substring(0, number.length() - 1);
        }

        String formattedNumber = trimmedNumber.replaceAll(NUMBER_FORMAT_REGEX, "$1" + mNumberSeparatorChar);

        if (formattedNumber.endsWith(mNumberSeparatorString)) {
            formattedNumber = formattedNumber.substring(0, formattedNumber.length() - 1);
        }

        return formattedNumber;
    }

    @NonNull
    @Override
    public String maskNumber(@NonNull String number) {
        final int length = number.length();
        final String lastDigits = number.substring(Math.max(length - VISIBLE_CHARS_MASK, 0));
        final String padded = String.format("%1$8s", lastDigits).replaceAll("\\s", String.valueOf(MASKING_CHAR));

        return padded.replaceFirst("(.{4})", "$1" + NON_BREAKING_SPACE);
    }
}
