/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 14/1/2021.
 */

package com.adyen.checkout.cse;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.adyen.checkout.core.exception.NoConstructorException;
import com.adyen.checkout.cse.exception.EncryptionException;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

// UnsynchronizedStaticDateFormatter is a deprecated check.
@SuppressWarnings("PMD.UnsynchronizedStaticDateFormatter")
public final class CardEncrypter {

    private static final String CARD_NUMBER_KEY = "number";
    private static final String EXPIRY_MONTH_KEY = "expiryMonth";
    private static final String EXPIRY_YEAR_KEY = "expiryYear";
    private static final String CVC_KEY = "cvc";
    private static final String HOLDER_NAME_KEY = "holderName";
    static final String GENERATION_TIME_KEY = "generationtime";

    private static final String BIN_KEY = "binValue";

    static final SimpleDateFormat GENERATION_DATE_FORMAT;

    static {
        GENERATION_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        GENERATION_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private CardEncrypter() {
        throw new NoConstructorException();
    }

    /**
     * Encrypts the available card data from {@link UnencryptedCard} into individual encrypted blocks.
     *
     * @param unencryptedCard The card data to be encrypted.
     * @param publicKey     The key to be used for encryption.
     * @return An {@link EncryptedCard} object with each encrypted field.
     * @throws EncryptionException in case the encryption fails.
     */
    @WorkerThread
    @NonNull
    public static EncryptedCard encryptFields(
            @NonNull final UnencryptedCard unencryptedCard,
            @NonNull final String publicKey
    ) throws EncryptionException {
        try {
            final String encryptedNumber;
            final String encryptedExpiryMonth;
            final String encryptedExpiryYear;
            final String encryptedSecurityCode;

            if (unencryptedCard.getNumber() != null) {
                encryptedNumber = GenericEncrypter.encryptField(
                        CARD_NUMBER_KEY,
                        unencryptedCard.getNumber(),
                        publicKey
                );
            } else {
                encryptedNumber = null;
            }

            if (unencryptedCard.getExpiryMonth() != null && unencryptedCard.getExpiryYear() != null) {
                encryptedExpiryMonth = GenericEncrypter.encryptField(
                        EXPIRY_MONTH_KEY,
                        unencryptedCard.getExpiryMonth(),
                        publicKey
                );

                encryptedExpiryYear = GenericEncrypter.encryptField(
                        EXPIRY_YEAR_KEY,
                        unencryptedCard.getExpiryYear(),
                        publicKey
                );
            } else if (unencryptedCard.getExpiryMonth() == null && unencryptedCard.getExpiryYear() == null) {
                encryptedExpiryMonth = null;
                encryptedExpiryYear = null;
            } else {
                throw new EncryptionException("Both expiryMonth and expiryYear need to be set for encryption.", null);
            }

            if (unencryptedCard.getCvc() != null) {
                encryptedSecurityCode = GenericEncrypter.encryptField(CVC_KEY, unencryptedCard.getCvc(), publicKey);
            } else {
                encryptedSecurityCode = null;
            }

            return new EncryptedCard(encryptedNumber, encryptedExpiryMonth, encryptedExpiryYear, encryptedSecurityCode);

        } catch (EncryptionException | IllegalStateException e) {
            throw new EncryptionException(e.getMessage() == null ? "No message." : e.getMessage(), e);
        }
    }

    /**
     * Encrypts all the card data present in {@link UnencryptedCard} into a single block of content.
     *
     * @param unencryptedCard The card data to be encrypted.
     * @param publicKey     The key to be used for encryption.
     * @return The encrypted card data String.
     * @throws EncryptionException in case the encryption fails.
     */
    @NonNull
    @WorkerThread
    public static String encrypt(
            @NonNull final UnencryptedCard unencryptedCard,
            @NonNull final  String publicKey
    ) throws EncryptionException {
        final JSONObject cardJson = new JSONObject();

        try {
            cardJson.put(CARD_NUMBER_KEY, unencryptedCard.getNumber());
            cardJson.put(EXPIRY_MONTH_KEY, unencryptedCard.getExpiryMonth());
            cardJson.put(EXPIRY_YEAR_KEY, unencryptedCard.getExpiryYear());
            cardJson.put(CVC_KEY, unencryptedCard.getCvc());
            cardJson.put(HOLDER_NAME_KEY, unencryptedCard.getCardHolderName());

            final String formattedGenerationTime = GenericEncrypter.makeGenerationTime(unencryptedCard.getGenerationTime());
            cardJson.put(GENERATION_TIME_KEY, formattedGenerationTime);

            final ClientSideEncrypter encrypter = new ClientSideEncrypter(publicKey);
            return encrypter.encrypt(cardJson.toString());
        } catch (JSONException e) {
            throw new EncryptionException("Failed to created encrypted JSON data.", e);
        }
    }

    /**
     * Encrypts the BIN of the card to be used in the Bin Lookup endpoint.
     *
     * @param bin The BIN value to be encrypted.
     * @param publicKey The key to be used for encryption.
     * @return The encrypted bin String.
     * @throws EncryptionException in case the encryption fails.
     */
    @NonNull
    @WorkerThread
    public static String encryptBin(@NonNull String bin, @NonNull String publicKey) throws EncryptionException {
        try {
            final JSONObject binJson = new JSONObject();
            binJson.put(BIN_KEY, bin);
            binJson.put(GENERATION_TIME_KEY, GenericEncrypter.makeGenerationTime(new Date()));

            final ClientSideEncrypter encrypter = new ClientSideEncrypter(publicKey);
            return encrypter.encrypt(binJson.toString());
        } catch (JSONException e) {
            throw new EncryptionException("Failed to created encrypted JSON data.", e);
        }
    }
}
