/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 18/3/2019.
 */

package com.adyen.checkout.card.data.validator;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.exeption.NoConstructorException;

final class ValidatorUtils {

    @NonNull
    static String normalize(@NonNull String value, @NonNull char... additionalCharsToReplace) {
        return value.replaceAll("[\\s" + new String(additionalCharsToReplace) + "]", "");
    }

    static boolean isDigitsAndSeparatorsOnly(@NonNull String value, @NonNull char... separators) {
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);

            for (char separator : separators) {
                if (!Character.isDigit(c) && separator != c) {
                    return false;
                }
            }
        }

        return true;
    }

    private ValidatorUtils() {
        throw new NoConstructorException();
    }
}
