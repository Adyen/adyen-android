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

public interface ExpiryDateValidator {
    /**
     * Validate an expiry date.
     *
     * @param expiryDate The expiry date to be validated.
     * @return An {@link ExpiryDateValidationResult}.
     */
    @NonNull
    ExpiryDateValidationResult validateExpiryDate(@NonNull String expiryDate);

    /**
     * {@link ValidationResult} for an expiry date.
     */
    final class ExpiryDateValidationResult extends ValidationResult {
        private final Integer mExpiryMonth;

        private final Integer mExpiryYear;

        public ExpiryDateValidationResult(@NonNull Validity validity, @Nullable Integer expiryMonth, @Nullable Integer expiryYear) {
            super(validity);

            mExpiryMonth = expiryMonth;
            mExpiryYear = expiryYear;
        }

        /**
         * @return The expiry month.
         */
        @Nullable
        public Integer getExpiryMonth() {
            return mExpiryMonth;
        }

        /**
         * @return The expiry year.
         */
        @Nullable
        public Integer getExpiryYear() {
            return mExpiryYear;
        }
    }
}
