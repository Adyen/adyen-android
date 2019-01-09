/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 03/05/2018.
 */

package com.adyen.checkout.ui.internal.common.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Matcher;

public final class PhoneNumberUtil {
    @NonNull
    public static ValidationResult validate(@NonNull String phoneNumber, boolean isRequired) {
        String trimmedPhoneNumber = phoneNumber.trim();
        Matcher matcher = Patterns.PHONE.matcher(trimmedPhoneNumber);
        boolean validPhoneNumber = matcher.matches();

        if (validPhoneNumber) {
            return new ValidationResult(Validity.VALID, trimmedPhoneNumber);
        } else if (matcher.hitEnd()) {
            Validity validity;
            String normalizedPhoneNumber;

            if (TextUtils.isEmpty(trimmedPhoneNumber)) {
                if (isRequired) {
                    validity = Validity.PARTIAL;
                    normalizedPhoneNumber = trimmedPhoneNumber;
                } else {
                    validity = Validity.VALID;
                    normalizedPhoneNumber = null;
                }
            } else {
                validity = Validity.PARTIAL;
                normalizedPhoneNumber = trimmedPhoneNumber;
            }

            return new ValidationResult(validity, normalizedPhoneNumber);
        } else {
            return new ValidationResult(Validity.INVALID, null);
        }
    }

    private PhoneNumberUtil() {
        throw new IllegalStateException("No instances.");
    }

    public enum Validity {
        VALID,
        PARTIAL,
        INVALID
    }

    public static final class ValidationResult {
        private final Validity mValidity;

        private final String mPhoneNumber;

        private ValidationResult(@NonNull Validity validity, @Nullable String phoneNumber) {
            mValidity = validity;
            mPhoneNumber = phoneNumber;
        }

        @NonNull
        public Validity getValidity() {
            return mValidity;
        }

        @Nullable
        public String getPhoneNumber() {
            return mPhoneNumber;
        }
    }
}
