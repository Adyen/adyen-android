/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/3/2021.
 */

package com.adyen.checkout.cse;

import androidx.annotation.NonNull;

import com.adyen.checkout.core.exception.NoConstructorException;

import java.util.regex.Pattern;

public final class ValidationUtils {

    private static final String PUBLIC_KEY_PATTERN = "([A-F]|[0-9]){5}\\|([A-F]|[0-9]){512}";
    private static final int PUBLIC_KEY_SIZE = 5 + 1 + 512;

    /**
     * Checks if the public key for encryption is valid.
     *
     * @param publicKey The public key string
     * @return True if valid, False if not.
     */
    public static boolean isPublicKeyValid(@NonNull String publicKey) {
        final Pattern pubKeyPattern = Pattern.compile(PUBLIC_KEY_PATTERN);
        return pubKeyPattern.matcher(publicKey).find() && publicKey.length() == PUBLIC_KEY_SIZE;
    }

    private ValidationUtils() {
        throw new NoConstructorException();
    }
}
