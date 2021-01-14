/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 6/6/2019.
 */

package com.adyen.checkout.cse.internal;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.adyen.adyencse.encrypter.exception.EncrypterException;
import com.adyen.checkout.cse.UnencryptedCard;
import com.adyen.checkout.cse.CardEncryptor;
import com.adyen.checkout.cse.EncryptedCard;
import com.adyen.checkout.cse.EncryptionException;

import java.util.Date;

public final class CardEncryptorImpl implements CardEncryptor {

    @SuppressWarnings("PMD.PreserveStackTrace")
    @WorkerThread
    @NonNull
    @Override
    public EncryptedCard encryptFields(@NonNull final UnencryptedCard unencryptedCard, @NonNull final String publicKey) throws EncryptionException {
        try {
            final Date generationTime = new Date();
            final String cardNumber = unencryptedCard.getNumber();
            String encryptedNumber = null;

            if (cardNumber != null) {
                try {
                    encryptedNumber = new com.adyen.adyencse.pojo.Card.Builder()
                            .setNumber(cardNumber)
                            .setGenerationTime(generationTime)
                            .build()
                            .serialize(publicKey);
                } catch (RuntimeException e) {
                    throw new EncryptionException("Encryption failed.", e);
                }
            }

            final Integer expiryMonth = unencryptedCard.getExpiryMonth();
            final Integer expiryYear = unencryptedCard.getExpiryYear();

            final String encryptedExpiryMonth;
            final String encryptedExpiryYear;

            if (expiryMonth != null && expiryYear != null) {
                encryptedExpiryMonth = new com.adyen.adyencse.pojo.Card.Builder()
                        .setExpiryMonth(String.valueOf(expiryMonth))
                        .setGenerationTime(generationTime)
                        .build()
                        .serialize(publicKey);
                encryptedExpiryYear = new com.adyen.adyencse.pojo.Card.Builder()
                        .setExpiryYear(String.valueOf(expiryYear))
                        .setGenerationTime(generationTime)
                        .build()
                        .serialize(publicKey);
            } else if (expiryMonth == null && expiryYear == null) {
                encryptedExpiryMonth = null;
                encryptedExpiryYear = null;
            } else {
                throw new EncryptionException("Both expiryMonth and expiryYear need to be set for encryption.", null);
            }

            final String encryptedSecurityCode = new com.adyen.adyencse.pojo.Card.Builder()
                    .setCvc(unencryptedCard.getSecurityCode())
                    .setGenerationTime(generationTime)
                    .build()
                    .serialize(publicKey);

            final EncryptedCard.Builder builder = new EncryptedCard.Builder().setEncryptedNumber(encryptedNumber);

            if (encryptedExpiryMonth != null && encryptedExpiryYear != null) {
                builder.setEncryptedExpiryDate(encryptedExpiryMonth, encryptedExpiryYear);
            } else {
                builder.clearEncryptedExpiryDate();
            }

            return builder
                    .setEncryptedSecurityCode(encryptedSecurityCode)
                    .build();
        } catch (EncrypterException | IllegalStateException e) {
            throw new EncryptionException(e.getMessage(), e.getCause());
        }
    }

    @NonNull
    @WorkerThread
    @Override
    public String encrypt(
            @NonNull final String holderName,
            @NonNull final UnencryptedCard unencryptedCard,
            final @NonNull String publicKey
    ) throws EncrypterException {

        final Date generationTime = new Date();
        final Integer expiryMonth = unencryptedCard.getExpiryMonth();
        final Integer expiryYear = unencryptedCard.getExpiryYear();

        final String expiryMonthString = expiryMonth != null ? String.valueOf(expiryMonth) : null;
        final String expiryYearString = expiryYear != null ? String.valueOf(expiryYear) : null;

        return new com.adyen.adyencse.pojo.Card.Builder()
                .setHolderName(holderName)
                .setNumber(unencryptedCard.getNumber())
                .setExpiryMonth(expiryMonthString)
                .setExpiryYear(expiryYearString)
                .setCvc(unencryptedCard.getSecurityCode())
                .setGenerationTime(generationTime)
                .build()
                .serialize(publicKey);
    }
}
