/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 6/12/2019.
 */

package com.adyen.checkout.base.util;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.exception.NoConstructorException;

import java.util.regex.Pattern;

public final class ValidationUtils {
    private static final int MIN_PHONE_NUMBER_DIGIT = 6;

    private static final String EXPRESSION = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
    private static final Pattern PATTERN = Pattern.compile(EXPRESSION, Pattern.CASE_INSENSITIVE);

    private ValidationUtils() {
        throw new NoConstructorException();
    }

    /**
     * check if phone number length more than {@value MIN_PHONE_NUMBER_DIGIT}.
     */
    @NonNull
    public static boolean isPhoneNumberValid(@NonNull String phoneNumber) {
        return phoneNumber.length() >= MIN_PHONE_NUMBER_DIGIT;
    }

    /**
     * check if email is valid.
     */
    @NonNull
    public static boolean isEmailValid(@NonNull String emailAddress) {
        return PATTERN.matcher(emailAddress).matches();
    }
}

