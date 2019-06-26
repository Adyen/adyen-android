/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 14/3/2019.
 */

package com.adyen.checkout.card.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class EncryptedCard {

    private String mEncryptedNumber;

    private String mEncryptedExpiryMonth;

    private String mEncryptedExpiryYear;

    private String mEncryptedSecurityCode;

    private EncryptedCard() {
        // Use builder.
    }

    @Nullable
    public String getEncryptedNumber() {
        return mEncryptedNumber;
    }

    @Nullable
    public String getEncryptedExpiryMonth() {
        return mEncryptedExpiryMonth;
    }

    @Nullable
    public String getEncryptedExpiryYear() {
        return mEncryptedExpiryYear;
    }

    @Nullable
    public String getEncryptedSecurityCode() {
        return mEncryptedSecurityCode;
    }

    /**
     * Builder for {@link EncryptedCard}s.
     */
    @SuppressWarnings("SyntheticAccessor")
    public static final class Builder {
        private final EncryptedCard mEncryptedCard = new EncryptedCard();

        /**
         * Set encrypted number.
         *
         * @param encryptedNumber {@link String}
         * @return {@link EncryptedCard.Builder}
         */
        @NonNull
        public Builder setEncryptedNumber(@Nullable String encryptedNumber) {
            mEncryptedCard.mEncryptedNumber = encryptedNumber;

            return this;
        }

        /**
         * Set encrypted expiry date.
         *
         * @param encryptedExpiryMonth expiry month {@link String}
         * @param encryptedExpiryYear  expiry year {@link String}
         * @return {@link EncryptedCard.Builder}
         */
        @NonNull
        public Builder setEncryptedExpiryDate(@NonNull String encryptedExpiryMonth, @NonNull String encryptedExpiryYear) {
            mEncryptedCard.mEncryptedExpiryMonth = encryptedExpiryMonth;
            mEncryptedCard.mEncryptedExpiryYear = encryptedExpiryYear;

            return this;
        }

        /**
         * Set encrypted security code.
         *
         * @param encryptedSecurityCode expiry year {@link String}
         * @return {@link EncryptedCard.Builder}
         */
        @NonNull
        public Builder setEncryptedSecurityCode(@Nullable String encryptedSecurityCode) {
            mEncryptedCard.mEncryptedSecurityCode = encryptedSecurityCode;

            return this;
        }

        /**
         * Build {@link EncryptedCard}.
         *
         * @return {@link EncryptedCard.Builder}
         */
        @NonNull
        public EncryptedCard build() {
            return mEncryptedCard;
        }
    }
}
