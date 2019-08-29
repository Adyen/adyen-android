/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 6/6/2019.
 */

package com.adyen.checkout.cse;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.exeption.CheckoutException;

public class EncryptionException extends CheckoutException {

    private static final long serialVersionUID = 604047691381396990L;

    public EncryptionException(@NonNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
