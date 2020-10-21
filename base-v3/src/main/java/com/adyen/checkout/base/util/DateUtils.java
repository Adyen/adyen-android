/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 30/7/2019.
 */

package com.adyen.checkout.base.util;

import androidx.annotation.NonNull;

import com.adyen.checkout.core.exception.NoConstructorException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("PMD.UnsynchronizedStaticDateFormatter")
public final class DateUtils {

    private static final int FOUR_DIGIT = 4;
    private static final int START_OF_FOUR_DIGIT_INDEX = 2;
    private static final SimpleDateFormat SERVER_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private DateUtils() {
        throw new NoConstructorException();
    }

    /**
     * Convert 4 digit year to 2 digit.
     */
    @NonNull
    public static String removeFirstTwoDigitFromYear(@NonNull String fourDigitYear) {
        if (fourDigitYear.length() == FOUR_DIGIT) {
            return fourDigitYear.substring(START_OF_FOUR_DIGIT_INDEX, FOUR_DIGIT);
        }
        return fourDigitYear;
    }

    /**
     * Parse server date format.
     */
    @NonNull
    public static Calendar parseServerDateFormat(@NonNull String dateValue) {
        final Calendar calendar = Calendar.getInstance();
        try {
            final Date date = SERVER_DATE_FORMAT.parse(dateValue);
            calendar.setTime(date);
            return calendar;
        } catch (ParseException e) {
            return calendar;
        }
    }

    /**
     * Convert to server date format.
     */
    @NonNull
    public static String toServerDateFormat(@NonNull Calendar calendar) {
        return SERVER_DATE_FORMAT.format(calendar.getTime());
    }
}
