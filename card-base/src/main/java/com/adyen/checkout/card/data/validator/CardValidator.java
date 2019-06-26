/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 15/3/2019.
 */

package com.adyen.checkout.card.data.validator;

import android.support.annotation.NonNull;

public interface CardValidator extends HolderNameValidator, NumberValidator, ExpiryDateValidator, SecurityCodeValidator {
    final class Builder {
        private static final char DEFAULT_NUMBER_SEPARATOR = ' ';
        private static final char DEFAULT_EXPIRY_DATE_SEPARATOR = '/';

        private HolderNameValidator mHolderNameValidator;
        private NumberValidator mNumberValidator;
        private ExpiryDateValidator mExpiryDateValidator;
        private SecurityCodeValidator mSecurityCodeValidator;

        @NonNull
        public Builder setHolderNameValidator(@NonNull HolderNameValidator validator) {
            mHolderNameValidator = validator;
            return this;
        }

        @NonNull
        public Builder setNumberValidator(@NonNull NumberValidator validator) {
            mNumberValidator = validator;
            return this;
        }

        @NonNull
        public Builder setExpiryDateValidator(@NonNull ExpiryDateValidator validator) {
            mExpiryDateValidator = validator;
            return this;
        }

        @NonNull
        public Builder setSecurityCodeValidator(@NonNull SecurityCodeValidator validator) {
            mSecurityCodeValidator = validator;
            return this;
        }

        @NonNull
        public CardValidator build() {
            return new CardValidatorImpl(
                    mHolderNameValidator != null ? mHolderNameValidator : new HolderNameValidatorImpl(),
                    mNumberValidator != null ? mNumberValidator : new NumberValidatorImpl(DEFAULT_NUMBER_SEPARATOR),
                    mExpiryDateValidator != null ? mExpiryDateValidator : new ExpiryDateValidatorImpl(DEFAULT_EXPIRY_DATE_SEPARATOR),
                    mSecurityCodeValidator != null ? mSecurityCodeValidator : new SecurityCodeValidatorImpl()
            );
        }
    }
}
