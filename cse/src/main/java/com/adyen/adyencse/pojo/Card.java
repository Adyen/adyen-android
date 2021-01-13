package com.adyen.adyencse.pojo;

import android.util.Log;

import com.adyen.adyencse.encrypter.ClientSideEncrypter;
import com.adyen.adyencse.encrypter.exception.EncrypterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * Created by andrei on 8/8/16.
 */
public class Card {

    private static final String tag = com.adyen.adyencse.pojo.Card.class.getSimpleName();
    private static final SimpleDateFormat GENERATION_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private String number;
    private String expiryMonth;
    private String expiryYear;
    private String cardHolderName;
    private String cvc;
    private Date generationTime;

    static {
        GENERATION_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * @deprecated Use {@link com.adyen.adyencse.pojo.Card.Builder} instead.
     */
    @Deprecated
    public Card() {

    }

    public String getNumber() {
        return number;
    }

    @Deprecated
    public void setNumber(String number) {
        this.number = number;
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    @Deprecated
    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    @Deprecated
    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    @Deprecated
    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getCvc() {
        return cvc;
    }

    @Deprecated
    public void setCvc(String cvc) {
        this.cvc = cvc;
    }

    public Date getGenerationTime() {
        return generationTime;
    }

    @Deprecated
    public void setGenerationTime(Date generationTime) {
        this.generationTime = generationTime;
    }

    /**
     * Serializes and encrypts the data from the {@link com.adyen.adyencse.pojo.Card}.
     *
     * @param publicKey The public key to encrypt with.
     * @return The serialized and encrypted data from the {@link com.adyen.adyencse.pojo.Card}.
     * @throws EncrypterException If the {@link com.adyen.adyencse.pojo.Card} could not be encrypted.
     */
    public String serialize(String publicKey) throws EncrypterException {
        JSONObject cardJson = new JSONObject();
        String encryptedData = null;

        try {
            cardJson.put("generationtime", GENERATION_DATE_FORMAT.format(generationTime));
            cardJson.put("number", number);
            cardJson.put("holderName", cardHolderName);
            cardJson.put("cvc", cvc);
            cardJson.put("expiryMonth", expiryMonth);
            cardJson.put("expiryYear", expiryYear);

            encryptedData = encryptData(cardJson.toString(), publicKey);
        } catch (JSONException e) {
            Log.e(tag, e.getMessage(), e);
        }

        return encryptedData;
    }

    /**
     * @return masked card number if the number is already available and the number of digits is longer than 13. Otherwise empty string.
     */
    public String toMaskedCardNumber() {
        if (number == null || number.length() < 14) {
            return "";
        }
        StringBuilder sb = new StringBuilder(number.length());

        sb.append(getMaskingChars(number.length())).append(getLastFourDigitsFromCardNumber(number));
        return sb.toString();
    }

    private String getLastFourDigitsFromCardNumber(final String fullCardNumber) {
        if (fullCardNumber != null && fullCardNumber.length() >= 14) {
            return fullCardNumber.substring(fullCardNumber.length() - 4);
        }
        return "";
    }

    private String getMaskingChars(final int totalLength) {
        int charsToMask = totalLength - 4;
        if (charsToMask <= 0) {
            return "";
        }
        char[] mask = new char[charsToMask];
        while (charsToMask > 0) {
            charsToMask--;
            mask[charsToMask] = '*';
        }
        return new String(mask);
    }

    /*
    * Helper method that calls the ClientSideEncrypter encrypt method
    * */
    private String encryptData(String data, String publicKey) throws EncrypterException {
        String encryptedData = null;

        try {
            ClientSideEncrypter encrypter = new ClientSideEncrypter(publicKey);
            encryptedData = encrypter.encrypt(data);
        } catch (EncrypterException e) {
            throw e;
        }

        return encryptedData;
    }

    @Override
    public String toString() {
        JSONObject cardJson = new JSONObject();

        try {
            cardJson.put("generationtime", GENERATION_DATE_FORMAT.format(generationTime));
            if (number.length() >= 4) {
                cardJson.put("number", number.substring(0, 3));
            }
            cardJson.put("holderName", cardHolderName);
        } catch (JSONException e) {
            Log.e(tag, e.getMessage(), e);
        }

        return cardJson.toString();
    }

    /**
     * Builder for {@link com.adyen.adyencse.pojo.Card} objects.
     */
    public static final class Builder {
        private final com.adyen.adyencse.pojo.Card card;

        public Builder() {
            card = new com.adyen.adyencse.pojo.Card();
        }

        /**
         * Set the mandatory generation time.
         *
         * @param generationTime The generation time.
         * @return The Builder instance.
         */
        public Builder setGenerationTime(Date generationTime) {
            card.generationTime = generationTime;

            return this;
        }

        /**
         * Set the optional card number.
         *
         * @param number The card number.
         * @return The Builder instance.
         */
        public Builder setNumber(String number) {
            card.number = removeWhiteSpaces(number);

            return this;
        }

        /**
         * Set the optional card holder name.
         *
         * @param holderName The holder name.
         * @return The Builder instance.
         */
        public Builder setHolderName(String holderName) {
            card.cardHolderName = trimAndRemoveMultipleWhiteSpaces(holderName);

            return this;
        }

        /**
         * Set the optional card security code.
         *
         * @param cvc The card security code.
         * @return The Builder instance.
         */
        public Builder setCvc(String cvc) {
            card.cvc = removeWhiteSpaces(cvc);

            return this;
        }

        /**
         * Set the optional expiry month, e.g. "1" or "01" for January.
         *
         * @param expiryMonth The expiry month.
         * @return The Builder instance.
         */
        public Builder setExpiryMonth(String expiryMonth) {
            card.expiryMonth = removeWhiteSpaces(expiryMonth);

            return this;
        }

        /**
         * Set the optional expiry year, e.g. "2021".
         *
         * @param expiryYear The expiry year.
         * @return The Builder instance.
         */
        public Builder setExpiryYear(String expiryYear) {
            card.expiryYear = removeWhiteSpaces(expiryYear);

            return this;
        }

        /**
         * Performs some simple checks on the given {@link com.adyen.adyencse.pojo.Card} object and builds it.
         *
         * @return The valid {@link com.adyen.adyencse.pojo.Card} object.
         * @throws NullPointerException If any mandatory field is null.
         * @throws IllegalStateException If any field is in an illegal state.
         */
        public com.adyen.adyencse.pojo.Card build() throws NullPointerException, IllegalStateException {
            requireNonNull(card.generationTime, "generationTime");
            require(card.number == null || card.number.matches("[0-9]{8,19}"), "number must be null or have 8 to 19 digits (inclusive).");
            require(card.cardHolderName == null || card.cardHolderName.length() > 0, "cardHolderName must be null or not empty.");
            require(card.cvc == null || (card.cvc.matches("[0-9]{3,4}")), "cvc must be null or have 3 to 4 digits.");
            require(card.expiryMonth == null || card.expiryMonth.matches("0?[1-9]|1[0-2]"), "expiryMonth must be null or between 1 and 12.");
            require(card.expiryYear == null
                    || card.expiryYear.matches("20\\d{2}"), "expiryYear must be in the second millennium and first century.");

            return card;
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

        private void requireNonNull(Object object, String objectName) throws IllegalStateException {
            if (object == null) {
                throw new NullPointerException(String.format("%s may not be null.", objectName));
            }
        }
    }
}
