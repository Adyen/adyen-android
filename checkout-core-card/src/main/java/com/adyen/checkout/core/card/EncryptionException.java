package com.adyen.checkout.core.card;

import android.support.annotation.NonNull;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 09/01/2018.
 */
public class EncryptionException extends Exception {
    public EncryptionException(@NonNull String message, @NonNull Throwable cause) {
        super(message, cause);
    }
}
