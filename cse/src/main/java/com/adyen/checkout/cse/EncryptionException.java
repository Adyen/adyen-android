/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 6/6/2019.
 */

package com.adyen.checkout.cse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.exception.CheckoutException;

public class EncryptionException extends CheckoutException {

    private static final long serialVersionUID = 604047691381396990L;

    public EncryptionException(@NonNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
