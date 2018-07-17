package com.adyen.checkout.core.card;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 06/02/2018.
 */
public interface CardValidator {
    int NUMBER_MINIMUM_LENGTH = 8;

    int NUMBER_MAXIMUM_LENGTH = 19;

    int SECURITY_CODE_MINIMUM_LENGTH = 3;

    int SECURITY_CODE_MAXIMUM_LENGTH = 4;

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
     * Validate a card number.
     *
     * @param number The card number to be validated.
     * @return A {@link NumberValidationResult}.
     */
    @NonNull
    NumberValidationResult validateNumber(@NonNull String number);

    /**
     * Validate an expiry date.
     *
     * @param expiryDate The expiry date to be validated.
     * @return An {@link ExpiryDateValidationResult}.
     */
    @NonNull
    ExpiryDateValidationResult validateExpiryDate(@NonNull String expiryDate);

    /**
     * Validate a security code.
     *
     * @param securityCode The security code to be validated.
     * @param isRequired Flag indicating whether the security code is required.
     * @return A {@link SecurityCodeValidationResult}.
     */
    @NonNull
    SecurityCodeValidationResult validateSecurityCode(@NonNull String securityCode, boolean isRequired, @Nullable CardType cardType);

    /**
     * Enum representing the validity of a card field.
     */
    enum Validity {
        VALID,
        PARTIAL,
        INVALID
    }

    /**
     * Class holding the result of a validation.
     */
    abstract class ValidationResult {
        private final Validity mValidity;

        private ValidationResult(@NonNull Validity validity) {
            mValidity = validity;
        }

        @NonNull
        public Validity getValidity() {
            return mValidity;
        }
    }

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
         * @return If {@code getValidity == Validity.VALID}, the normalized valid holder name.<br/>
         * If {@code getValidity == Validity.PARTIAL}, the normalized partial holder name.<br/>
         * If {@code getValidity == Validity.INVALID}, {@code null}.
         */
        @Nullable
        public String getHolderName() {
            return mHolderName;
        }
    }

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
         * @return If {@code getValidity == Validity.VALID}, the normalized valid number.<br/>
         * If {@code getValidity == Validity.PARTIAL}, the normalized partial number.<br/>
         * If {@code getValidity == Validity.INVALID}, {@code null}.
         */
        @Nullable
        public String getNumber() {
            return mNumber;
        }
    }

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
         * @return If {@code getValidity == Validity.VALID}, the valid expiry month, where {@code 1} refers to January.<br/>
         * If {@code getValidity == Validity.PARTIAL}, the valid expiry month or {@code null} if not yet provided.<br/>
         * If {@code getValidity == Validity.INVALID}, {@code null}.
         */
        @Nullable
        public Integer getExpiryMonth() {
            return mExpiryMonth;
        }

        /**
         * @return If {@code getValidity == Validity.VALID}, the valid expiry year.<br/>
         * If {@code getValidity == Validity.PARTIAL}, {@code null}.<br/>
         * If {@code getValidity == Validity.INVALID}, {@code null}.
         */
        @Nullable
        public Integer getExpiryYear() {
            return mExpiryYear;
        }
    }

    /**
     * {@link ValidationResult} for a security code.
     */
    final class SecurityCodeValidationResult extends ValidationResult {
        private String mSecurityCode;

        public SecurityCodeValidationResult(@NonNull Validity validity, @Nullable String securityCode) {
            super(validity);

            mSecurityCode = securityCode;
        }

        /**
         * @return If {@code getValidity == Validity.VALID}, the normalized valid security code (may be {@code null}, only valid then if the security
         * code is optional).<br/>
         * If {@code getValidity == Validity.PARTIAL}, the normalized partial security code.<br/>
         * If {@code getValidity == Validity.INVALID}, {@code null}.
         */
        @Nullable
        public String getSecurityCode() {
            return mSecurityCode;
        }
    }
}
