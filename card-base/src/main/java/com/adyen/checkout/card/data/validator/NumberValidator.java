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

public interface NumberValidator {
    int NUMBER_MINIMUM_LENGTH = 8;
    int NUMBER_MAXIMUM_LENGTH = 19;

    int GENERAL_CARD_NUMBER_SIZE = 16;
    int AMEX_NUMBER_SIZE = 15;

    /**
     * Validate a card number.
     *
     * @param number The card number to be validated.
     * @return A {@link NumberValidationResult}.
     */
    @NonNull
    NumberValidationResult validateNumber(@NonNull String number);

    /**
     * {@link ValidationResult} for a card number.
     */
    final class NumberValidationResult extends ValidationResult {
        private final String mNumber;

        public NumberValidationResult(@NonNull Validity validity, @Nullable String number) {
            super(validity);

            mNumber = number;
        }

        /**
         * @return The card number.
         */
        @Nullable
        public String getNumber() {
            return mNumber;
        }
    }
}
