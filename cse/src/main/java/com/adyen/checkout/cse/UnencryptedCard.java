/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 14/1/2021.
 */

package com.adyen.checkout.cse;

import static com.adyen.checkout.cse.CardEncrypter.GENERATION_DATE_FORMAT;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;


public class UnencryptedCard {

    private static final String tag = UnencryptedCard.class.getSimpleName();

    private final String number;
    private final String expiryMonth;
    private final String expiryYear;
    private final String cvc;
    private final String cardHolderName;
    private final Date generationTime;

    public UnencryptedCard(
            @Nullable String number,
            @Nullable String expiryMonth,
            @Nullable String expiryYear,
            @Nullable String cvc,
            @Nullable String cardHolderName,
            @Nullable Date generationTime
    ) {
        this.number = number;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cvc = cvc;
        this.cardHolderName = cardHolderName;
        this.generationTime = generationTime;
    }

    @Nullable
    public String getNumber() {
        return number;
    }

    @Nullable
    public String getExpiryMonth() {
        return expiryMonth;
    }

    @Nullable
    public String getExpiryYear() {
        return expiryYear;
    }

    @Nullable
    public String getCvc() {
        return cvc;
    }

    @Nullable
    public String getCardHolderName() {
        return cardHolderName;
    }

    @Nullable
    public Date getGenerationTime() {
        return generationTime;
    }

    @NonNull
    @Override
    public String toString() {
        JSONObject cardJson = new JSONObject();

        try {
            if (generationTime != null) {
                cardJson.put("generationtime", GENERATION_DATE_FORMAT.format(generationTime));
            }
            if (number != null) {
                // Builder checks that number needs to be at least 8 digits.
                cardJson.put("number", number.substring(0, 3));
            }
            cardJson.putOpt("holderName", cardHolderName);
        } catch (JSONException e) {
            Log.e(tag, e.getMessage(), e);
        }

        return cardJson.toString();
    }

    /**
     * Builder for {@link UnencryptedCard} objects.
     */
    public static final class Builder {
        private String number;
        private String expiryMonth;
        private String expiryYear;
        private String cardHolderName;
        private String cvc;
        private Date generationTime;

        /**
         * Set the optional card number.
         *
         * @param number The card number.
         * @return The Builder instance.
         */
        public Builder setNumber(@NonNull String number) {
            this.number = removeWhiteSpaces(number);

            return this;
        }

        /**
         * Set the optional expiry month, e.g. "1" or "01" for January.
         *
         * @param expiryMonth The expiry month.
         * @return The Builder instance.
         */
        public Builder setExpiryMonth(@NonNull String expiryMonth) {
            this.expiryMonth = removeWhiteSpaces(expiryMonth);

            return this;
        }

        /**
         * Set the optional expiry year, e.g. "2021".
         *
         * @param expiryYear The expiry year.
         * @return The Builder instance.
         */
        public Builder setExpiryYear(@NonNull String expiryYear) {
            this.expiryYear = removeWhiteSpaces(expiryYear);

            return this;
        }

        /**
         * Set the optional card security code.
         *
         * @param cvc The card security code.
         * @return The Builder instance.
         */
        public Builder setCvc(@NonNull String cvc) {
            this.cvc = removeWhiteSpaces(cvc);

            return this;
        }

        /**
         * Set the optional card holder name.
         *
         * @param holderName The holder name.
         * @return The Builder instance.
         */
        public Builder setHolderName(@NonNull String holderName) {
            this.cardHolderName = trimAndRemoveMultipleWhiteSpaces(holderName);

            return this;
        }

        /**
         * Set the mandatory generation time.
         *
         * @param generationTime The generation time.
         * @return The Builder instance.
         */
        public Builder setGenerationTime(Date generationTime) {
            this.generationTime = generationTime;

            return this;
        }

        /**
         * Performs some simple checks on the given {@link UnencryptedCard} object and builds it.
         *
         * @return The valid {@link UnencryptedCard} object.
         * @throws NullPointerException If any mandatory field is null.
         * @throws IllegalStateException If any field is in an illegal state.
         */
        public UnencryptedCard build() throws NullPointerException, IllegalStateException {
            require(number == null || number.matches("[0-9]{8,19}"),
                    "number must be null or have 8 to 19 digits (inclusive).");
            require(cardHolderName == null || cardHolderName.length() > 0,
                    "cardHolderName must be null or not empty.");
            require(cvc == null || (cvc.matches("[0-9]{3,4}")),
                    "cvc must be null or have 3 to 4 digits.");
            require(expiryMonth == null || expiryMonth.matches("0?[1-9]|1[0-2]"),
                    "expiryMonth must be null or between 1 and 12.");
            require(expiryYear == null || expiryYear.matches("20\\d{2}"),
                    "expiryYear must be in the second millennium and first century.");

            return new UnencryptedCard(number, expiryMonth, expiryYear, cvc, cardHolderName, generationTime);
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
