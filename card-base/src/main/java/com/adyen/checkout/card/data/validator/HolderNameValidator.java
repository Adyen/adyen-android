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

public interface HolderNameValidator {
    /**
     * Validate card holder name.
     *
     * @param holderName The holder name to be validated.
     * @param isRequired Flag indicating whether the holder name is required.
     * @return A {@link HolderNameValidationResult}.
     */
    @NonNull
    HolderNameValidationResult validateHolderName(@NonNull String holderName, boolean isRequired);

    /**
     * {@link ValidationResult} for a card holder name.
     */
    final class HolderNameValidationResult extends ValidationResult {
        private final String mHolderName;

        public HolderNameValidationResult(@NonNull Validity validity, @Nullable String holderName) {
            super(validity);

            mHolderName = holderName;
        }

        /**
         * @return holder name
         */
        @Nullable
        public String getHolderName() {
            return mHolderName;
        }
    }
}
