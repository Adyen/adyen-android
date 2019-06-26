/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 18/3/2019.
 */

package com.adyen.checkout.card.data.validator;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.component.validator.ValidationResult;
import com.adyen.checkout.base.component.validator.Validity;
import com.adyen.checkout.card.model.CardType;

public interface SecurityCodeValidator {
    int SECURITY_CODE_MINIMUM_LENGTH = 3;
    int SECURITY_CODE_MAXIMUM_LENGTH = 4;

    /**
     * Validate a security code.
     *
     * @param securityCode The security code to be validated.
     * @param isRequired   Flag indicating whether the security code is required.
     * @return A {@link SecurityCodeValidationResult}.
     */
    @NonNull
    SecurityCodeValidationResult validateSecurityCode(@NonNull String securityCode, boolean isRequired, @Nullable CardType cardType);

    /**
     * {@link ValidationResult} for a security code.
     */
    final class SecurityCodeValidationResult extends ValidationResult {
        private final String mSecurityCode;

        public SecurityCodeValidationResult(@NonNull Validity validity, @Nullable String securityCode) {
            super(validity);

            mSecurityCode = securityCode;
        }

        /**
         * @return The security code.
         */
        @Nullable
        public String getSecurityCode() {
            return mSecurityCode;
        }
    }
}
