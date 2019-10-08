/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 30/7/2019.
 */

package com.adyen.checkout.base.util;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.exception.NoConstructorException;

public final class DateUtils {

    private static final int FOUR_DIGIT = 4;
    private static final int START_OF_FOUR_DIGIT_INDEX = 2;

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
}
