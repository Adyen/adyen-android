/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 6/6/2019.
 */

package com.adyen.checkout.cse;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.util.concurrent.Callable;

import adyen.com.adyencse.encrypter.exception.EncrypterException;

public interface CardEncryptor {

    /**
     * Encrypt the individual fields of a {@link Card} to an {@link EncryptedCard}.
     *
     * @param card      The {@link Card} to be encrypted.
     * @param publicKey The public key to encrypt with.
     * @return A {@link Callable} object returning an {@link EncryptedCard}.
     */
    @NonNull
    @WorkerThread
    EncryptedCard encryptFields(@NonNull Card card, @NonNull String publicKey) throws EncryptionException;

    /**
     * Encrypt the {@link Card} to a token.
     *
     * @param holderName The name of the card holder.
     * @param card       The {@link Card} to be encrypted.
     * @param publicKey  The public key to encrypt with.
     * @return A {@link Callable} object returning an encrypted token.
     */
    @NonNull
    @WorkerThread
    String encrypt(@NonNull String holderName, @NonNull Card card, @NonNull String publicKey) throws EncrypterException;
}
