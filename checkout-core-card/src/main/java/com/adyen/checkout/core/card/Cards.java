/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 25/01/2018.
 */

package com.adyen.checkout.core.card;

import android.annotation.SuppressLint;
import android.app.Application;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.HostProvider;
import com.adyen.checkout.core.card.internal.CardEncryptorImpl;
import com.adyen.checkout.core.card.internal.CardFormatterImpl;
import com.adyen.checkout.core.card.internal.CardValidatorImpl;

import java.util.Date;
import java.util.concurrent.Callable;

public final class Cards {
    @NonNull
    public static final CardFormatter FORMATTER;

    @NonNull
    public static final CardValidator VALIDATOR;

    @NonNull
    public static final CardEncryptor ENCRYPTOR;

    static {
        char numberSeparator = ' ';
        char expiryDateSeparator = '/';

        VALIDATOR = new CardValidatorImpl(numberSeparator, expiryDateSeparator);
        FORMATTER = new CardFormatterImpl(numberSeparator, expiryDateSeparator);
        ENCRYPTOR = new CardEncryptorImpl();
    }

    /**
     * Fetches a public key and encrypts a {@link Card} to an {@link EncryptedCard}.
     *
     * @param application The current {@link Application}.
     * @param hostProvider The {@link HostProvider} indicating the environment to connect to.
     * @param publicKeyToken The public key token to retrieve the public key with.
     * @param card The {@link Card} to be encrypted.
     * @param generationTime The generation {@link Date} of the {@link Card}.
     * @return A {@link Callable} that returns the {@link EncryptedCard}.
     *
     * @see CardApi#getPublicKey(String)
     * @see CardEncryptor#encryptFields(Card, Date, String)
     */
    @SuppressLint("LambdaLast")
    @NonNull
    public static Callable<EncryptedCard> fetchPublicKeyAndEncrypt(
            @NonNull Application application,
            @NonNull HostProvider hostProvider,
            @NonNull final String publicKeyToken,
            @NonNull final Card card,
            @NonNull final Date generationTime
    ) {
        final CardApi cardApi = CardApi.getInstance(application, hostProvider);

        return new Callable<EncryptedCard>() {
            @Override
            public EncryptedCard call() throws Exception {
                String publicKey = cardApi.getPublicKey(publicKeyToken).call();

                return ENCRYPTOR.encryptFields(card, generationTime, publicKey).call();
            }
        };
    }

    /**
     * Fetches a public key and encrypts a {@link Card} to a token.
     *
     * @param application The current {@link Application}.
     * @param hostProvider The {@link HostProvider} indicating the environment to connect to.
     * @param holderName The name of the card holder.
     * @param publicKeyToken The public key token to retrieve the public key with.
     * @param card The {@link Card} to be encrypted.
     * @param generationTime The generation {@link Date} of the {@link Card}.
     * @return A {@link Callable} that returns a token representing the encrypted {@link Card}.
     *
     * @see CardApi#getPublicKey(String)
     * @see CardEncryptor#encrypt(String, Card, Date, String)
     */
    @SuppressLint("LambdaLast")
    @NonNull
    public static Callable<String> fetchPublicKeyAndEncryptToToken(
            @NonNull Application application,
            @NonNull HostProvider hostProvider,
            @NonNull final String holderName,
            @NonNull final String publicKeyToken,
            @NonNull final Card card,
            @NonNull final Date generationTime
    ) {
        final CardApi cardApi = CardApi.getInstance(application, hostProvider);

        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                String publicKey = cardApi.getPublicKey(publicKeyToken).call();

                return ENCRYPTOR.encrypt(holderName, card, generationTime, publicKey).call();
            }
        };
    }

    private Cards() {
        throw new IllegalStateException("No instances.");
    }
}
