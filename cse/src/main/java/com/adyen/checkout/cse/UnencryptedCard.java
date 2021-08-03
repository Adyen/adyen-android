/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 14/1/2021.
 */

package com.adyen.checkout.cse;

import static com.adyen.checkout.cse.CardEncrypter.GENERATION_DATE_FORMAT;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;


public class UnencryptedCard {

    private final String mNumber;
    private final String mExpiryMonth;
    private final String mExpiryYear;
    private final String mCvc;
    private final String mCardHolderName;
    private final Date mGenerationTime;
    private final String mCardPassword;
    private final String mTaxNumber;

    public UnencryptedCard(
            @Nullable String number,
            @Nullable String expiryMonth,
            @Nullable String expiryYear,
            @Nullable String cvc,
            @Nullable String cardHolderName,
            @Nullable Date generationTime,
            @Nullable String cardPassword,
            @Nullable String taxNumber
    ) {
        this.mNumber = number;
        this.mExpiryMonth = expiryMonth;
        this.mExpiryYear = expiryYear;
        this.mCvc = cvc;
        this.mCardHolderName = cardHolderName;
        this.mGenerationTime = generationTime;
        this.mCardPassword = cardPassword;
        this.mTaxNumber = taxNumber;
    }

    @Nullable
    public String getNumber() {
        return mNumber;
    }

    @Nullable
    public String getExpiryMonth() {
        return mExpiryMonth;
    }

    @Nullable
    public String getExpiryYear() {
        return mExpiryYear;
    }

    @Nullable
    public String getCvc() {
        return mCvc;
    }

    @Nullable
    public String getCardHolderName() {
        return mCardHolderName;
    }

    @Nullable
    public Date getGenerationTime() {
        return mGenerationTime;
    }

    @Nullable
    public String getCardPassword() {
        return mCardPassword;
    }

    @Nullable
    public String getTaxNumber() {
        return mTaxNumber;
    }

    @NonNull
    @Override
    public String toString() {
        final JSONObject cardJson = new JSONObject();

        try {
            if (mGenerationTime != null) {
                cardJson.put("generationtime", GENERATION_DATE_FORMAT.format(mGenerationTime));
            }
            if (mNumber != null) {
                // Builder checks that number needs to be at least 8 digits.
                final int firstThreeDigits = 3;
                cardJson.put("number", mNumber.substring(0, firstThreeDigits));
            }
            cardJson.putOpt("holderName", mCardHolderName);
        } catch (JSONException e) {
            throw new RuntimeException("UnencryptedCard toString() failed.", e);
        }

        return cardJson.toString();
    }

    /**
     * Builder for {@link UnencryptedCard} objects.
     */
    public static final class Builder {
        private String mNumber;
        private String mExpiryMonth;
        private String mExpiryYear;
        private String mCardHolderName;
        private String mCvc;
        private Date mGenerationTime;
        private String mCardPassword;
        private String mTaxNumber;

        /**
         * Set the optional card number.
         *
         * @param number The card number.
         * @return The Builder instance.
         */
        @NonNull
        public Builder setNumber(@NonNull String number) {
            this.mNumber = removeWhiteSpaces(number);
            return this;
        }

        /**
         * Set the optional expiry month, e.g. "1" or "01" for January.
         *
         * @param expiryMonth The expiry month.
         * @return The Builder instance.
         */
        @NonNull
        public Builder setExpiryMonth(@NonNull String expiryMonth) {
            this.mExpiryMonth = removeWhiteSpaces(expiryMonth);
            return this;
        }

        /**
         * Set the optional expiry year, e.g. "2021".
         *
         * @param expiryYear The expiry year.
         * @return The Builder instance.
         */
        @NonNull
        public Builder setExpiryYear(@NonNull String expiryYear) {
            this.mExpiryYear = removeWhiteSpaces(expiryYear);
            return this;
        }

        /**
         * Set the optional card security code.
         *
         * @param cvc The card security code.
         * @return The Builder instance.
         */
        @NonNull
        public Builder setCvc(@NonNull String cvc) {
            this.mCvc = removeWhiteSpaces(cvc);
            return this;
        }

        /**
         * Set the optional card holder name.
         *
         * @param holderName The holder name.
         * @return The Builder instance.
         */
        @NonNull
        public Builder setHolderName(@NonNull String holderName) {
            this.mCardHolderName = trimAndRemoveMultipleWhiteSpaces(holderName);
            return this;
        }

        /**
         * Set the mandatory generation time.
         *
         * @param generationTime The generation time.
         * @return The Builder instance.
         */
        @NonNull
        public Builder setGenerationTime(@NonNull Date generationTime) {
            this.mGenerationTime = generationTime;
            return this;
        }

        /**
         * Set the card password (First two digits).
         *
         * @param cardPassword The card password.
         * @return The Builder instance
         */
        @NonNull
        public Builder setCardPassword(@NonNull String cardPassword) {
            this.mCardPassword = cardPassword;
            return this;
        }

        /**
         * Set the tax number.
         *
         * @param taxNumber The tax number.
         * @return The Builder instance.
         */
        @NonNull
        public Builder setTaxNumber(@NonNull String taxNumber) {
            this.mTaxNumber = taxNumber;
            return this;
        }

        /**
         * Performs some simple checks on the given {@link UnencryptedCard} object and builds it.
         *
         * @return The valid {@link UnencryptedCard} object.
         * @throws NullPointerException If any mandatory field is null.
         * @throws IllegalStateException If any field is in an illegal state.
         */
        @NonNull
        public UnencryptedCard build() throws NullPointerException, IllegalStateException {
            require(mNumber == null || mNumber.matches("[0-9]{8,19}"),
                    "number must be null or have 8 to 19 digits (inclusive).");
            require(mCardHolderName == null || mCardHolderName.length() > 0,
                    "cardHolderName must be null or not empty.");
            require(mCvc == null || mCvc.matches("[0-9]{3,4}"),
                    "cvc must be null or have 3 to 4 digits.");
            require(mExpiryMonth == null || mExpiryMonth.matches("0?[1-9]|1[0-2]"),
                    "expiryMonth must be null or between 1 and 12.");
            require(mExpiryYear == null || mExpiryYear.matches("20\\d{2}"),
                    "expiryYear must be in the second millennium and first century.");

            return new UnencryptedCard(mNumber, mExpiryMonth, mExpiryYear, mCvc, mCardHolderName, mGenerationTime, mCardPassword, mTaxNumber);
        }

        private String removeWhiteSpaces(String string) {
            return string != null ? string.replaceAll("\\s", "") : null;
        }

        private String trimAndRemoveMultipleWhiteSpaces(String string) {
            return string != null ? string.trim().replaceAll("\\s{2,}", " ") : null;
        }

        private void require(boolean condition, String message) throws IllegalStateException {
            if (!condition) {
                throw new IllegalStateException(message);
            }
        }
    }
}
