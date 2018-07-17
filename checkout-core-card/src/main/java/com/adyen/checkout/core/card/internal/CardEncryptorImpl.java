package com.adyen.checkout.core.card.internal;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.card.Card;
import com.adyen.checkout.core.card.CardEncryptor;
import com.adyen.checkout.core.card.EncryptedCard;
import com.adyen.checkout.core.card.EncryptionException;

import java.util.Date;
import java.util.concurrent.Callable;

import adyen.com.adyencse.encrypter.exception.EncrypterException;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 06/02/2018.
 */
public final class CardEncryptorImpl implements CardEncryptor {
    @NonNull
    @Override
    public Callable<EncryptedCard> encryptFields(@NonNull final Card card, @NonNull final Date generationTime, @NonNull final String publicKey) {
        return new Callable<EncryptedCard>() {
            @Override
            public EncryptedCard call() throws Exception {
                try {
                    String cardNumber = card.getNumber();
                    String encryptedNumber = null;

                    if (cardNumber != null) {
                        encryptedNumber = new adyen.com.adyencse.pojo.Card.Builder()
                                .setNumber(cardNumber)
                                .setGenerationTime(generationTime)
                                .build()
                                .serialize(publicKey);
                    }

                    Integer expiryMonth = card.getExpiryMonth();
                    Integer expiryYear = card.getExpiryYear();

                    String encryptedExpiryMonth;
                    String encryptedExpiryYear;

                    if (expiryMonth != null && expiryYear != null) {
                        encryptedExpiryMonth = new adyen.com.adyencse.pojo.Card.Builder()
                                .setExpiryMonth(String.valueOf(expiryMonth))
                                .setGenerationTime(generationTime)
                                .build()
                                .serialize(publicKey);
                        encryptedExpiryYear = new adyen.com.adyencse.pojo.Card.Builder()
                                .setExpiryYear(String.valueOf(expiryYear))
                                .setGenerationTime(generationTime)
                                .build()
                                .serialize(publicKey);
                    } else if (expiryMonth == null && expiryYear == null) {
                        encryptedExpiryMonth = null;
                        encryptedExpiryYear = null;
                    } else {
                        throw new IllegalStateException("Both expiryMonth and expiryYear need to be set for encryption.");
                    }

                    String encryptedSecurityCode = new adyen.com.adyencse.pojo.Card.Builder()
                            .setCvc(card.getSecurityCode())
                            .setGenerationTime(generationTime)
                            .build()
                            .serialize(publicKey);

                    EncryptedCard.Builder builder = new EncryptedCard.Builder().setEncryptedNumber(encryptedNumber);

                    if (encryptedExpiryMonth != null && encryptedExpiryYear != null) {
                        builder.setEncryptedExpiryDate(encryptedExpiryMonth, encryptedExpiryYear);
                    } else {
                        builder.clearEncryptedExpiryDate();
                    }

                    return builder
                            .setEncryptedSecurityCode(encryptedSecurityCode)
                            .build();
                } catch (EncrypterException e) {
                    throw new EncryptionException(e.getMessage(), e.getCause());
                }
            }
        };
    }

    @NonNull
    @Override
    public Callable<String> encrypt(
            @NonNull final String holderName,
            @NonNull final Card card,
            @NonNull final Date generationTime,
            final @NonNull String publicKey
    ) {
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                Integer expiryMonth = card.getExpiryMonth();
                Integer expiryYear = card.getExpiryYear();

                String expiryMonthString = expiryMonth != null ? String.valueOf(expiryMonth) : null;
                String expiryYearString = expiryYear != null ? String.valueOf(expiryYear) : null;

                return new adyen.com.adyencse.pojo.Card.Builder()
                        .setHolderName(holderName)
                        .setNumber(card.getNumber())
                        .setExpiryMonth(expiryMonthString)
                        .setExpiryYear(expiryYearString)
                        .setCvc(card.getSecurityCode())
                        .setGenerationTime(generationTime)
                        .build()
                        .serialize(publicKey);
            }
        };
    }
}
