/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 18/3/2019.
 */

package com.adyen.checkout.card.data.validator;

import android.support.annotation.NonNull;

import com.adyen.checkout.base.component.validator.Validity;

public final class HolderNameValidatorImpl implements HolderNameValidator {
    @NonNull
    @Override
    public HolderNameValidationResult validateHolderName(@NonNull String holderName, boolean isRequired) {
        final String normalizedHolderName = holderName.trim();

        if (normalizedHolderName.isEmpty()) {
            final Validity validity = isRequired ? Validity.INVALID : Validity.VALID;

            return new HolderNameValidationResult(validity, null);
        } else {
            return new HolderNameValidationResult(Validity.VALID, normalizedHolderName);
        }
    }
}
