package com.adyen.checkout.core.card.internal;

import android.support.annotation.NonNull;
import android.text.TextWatcher;
import android.widget.EditText;

import com.adyen.checkout.core.card.CardFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 07/02/2018.
 */
public final class CardFormatterImpl implements CardFormatter {
    private static final char MASKING_CHAR = '\u2022';

    private static final char NON_BREAKING_SPACE = '\u00A0';

    private final char mNumberSeparator;

    private final char mExpiryDateSeparator;

    private final String mExpiryDateFormat;

    public CardFormatterImpl(char numberSeparator, char expiryDateSeparator) {
        mNumberSeparator = numberSeparator;
        mExpiryDateSeparator = expiryDateSeparator;
        mExpiryDateFormat = "MM" + expiryDateSeparator + "yy";
    }

    @NonNull
    @Override
    public String formatNumber(@NonNull String number) {
        String formattedNumber = number.replaceAll("(.{4})", "$1" + mNumberSeparator);

        if (formattedNumber.endsWith(String.valueOf(mNumberSeparator))) {
            formattedNumber = formattedNumber.substring(0, formattedNumber.length() - 1);
        }

        return formattedNumber;
    }

    @NonNull
    @Override
    public String maskNumber(@NonNull String number) {
        int length = number.length();
        String lastDigits = number.substring(Math.max(length - 4, 0));
        String padded = String.format("%1$8s", lastDigits).replaceAll("\\s", String.valueOf(MASKING_CHAR));

        return padded.replaceFirst("(.{4})", "$1" + NON_BREAKING_SPACE);
    }

    @NonNull
    @Override
    public TextWatcher attachAsYouTypeNumberFormatter(@NonNull EditText editText) {
        return AsYouTypeCardNumberFormatter.attach(editText, mNumberSeparator);
    }

    @NonNull
    @Override
    public String formatExpiryDate(int expiryMonth, int expiryYear) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.MONTH, expiryMonth - 1);
        calendar.set(Calendar.YEAR, expiryYear);
        Date date = calendar.getTime();

        return new SimpleDateFormat(mExpiryDateFormat, Locale.US).format(date);
    }

    @NonNull
    @Override
    public TextWatcher attachAsYouTypeExpiryDateFormatter(@NonNull EditText editText) {
        return AsYouTypeExpiryDateFormatter.attach(editText, mExpiryDateSeparator);
    }

    @NonNull
    @Override
    public String formatSecurityCode(@NonNull String securityCode) {
        return securityCode.trim();
    }
}
