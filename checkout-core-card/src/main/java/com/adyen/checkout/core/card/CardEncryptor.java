/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 06/02/2018.
 */

package com.adyen.checkout.core.card;

import android.support.annotation.NonNull;

import java.util.Date;
import java.util.concurrent.Callable;

public interface CardEncryptor {
    /**
     * Encrypt the individual fields of a {@link Card} to an {@link EncryptedCard}.
     *
     * @param card The {@link Card} to be encrypted.
     * @param generationTime The generation {@link Date}.
     * @param publicKey The public key to encrypt with.
     * @return A {@link Callable} object returning an {@link EncryptedCard}.
     */
    @NonNull
    Callable<EncryptedCard> encryptFields(@NonNull Card card, @NonNull Date generationTime, @NonNull String publicKey);

    /**
     * Encrypt the {@link Card} to a token.
     *
     * @param holderName The name of the card holder.
     * @param card The {@link Card} to be encrypted.
     * @param generationTime The generation {@link Date}.
     * @param publicKey The public key to encrypt with.
     * @return A {@link Callable} object returning an encrypted token.
     */
    @NonNull
    Callable<String> encrypt(@NonNull String holderName, @NonNull Card card, @NonNull Date generationTime, @NonNull String publicKey);
}
