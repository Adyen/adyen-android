/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 09/01/2018.
 */

package com.adyen.checkout.core.card;

import android.support.annotation.NonNull;

public class EncryptionException extends Exception {
    public EncryptionException(@NonNull String message, @NonNull Throwable cause) {
        super(message, cause);
    }
}
