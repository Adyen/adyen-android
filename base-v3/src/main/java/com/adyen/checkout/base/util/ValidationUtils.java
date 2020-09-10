/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 6/12/2019.
 */

package com.adyen.checkout.base.util;

import androidx.annotation.NonNull;

import com.adyen.checkout.core.exception.NoConstructorException;

import java.util.regex.Pattern;

public final class ValidationUtils {

    private static final String EMAIL_REGEX = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);

    private static final String PHONE_REGEX = "^\\D*(\\d\\D*){9,14}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

    private static final String CLIENT_KEY_REGEX = "([a-z]){4}\\_([A-z]|\\d){32}";
    private static final Pattern CLIENT_KEY_PATTERN = Pattern.compile(CLIENT_KEY_REGEX);

    private ValidationUtils() {
        throw new NoConstructorException();
    }

    /**
     * Check if phone number is valid.
     *
     * @param phoneNumber A string to check if is a phone number.
     * @return If we consider it a valid phone number or not.
     */
    @NonNull
    public static boolean isPhoneNumberValid(@NonNull String phoneNumber) {
        return PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    /**
     * Check if email is valid.
     *
     * @param emailAddress A string to check if is an email address.
     * @return If we consider it a valid email or not.
     */
    @NonNull
    public static boolean isEmailValid(@NonNull String emailAddress) {
        return EMAIL_PATTERN.matcher(emailAddress).matches();
    }

    /**
     * Check if the Client Key is valid.
     *
     * @param clientKey A string to check if is a client key.
     * @return If we consider it a valid client key or not.
     */
    public static boolean isClientKeyValid(@NonNull String clientKey) {
        return CLIENT_KEY_PATTERN.matcher(clientKey).matches();
    }
}

