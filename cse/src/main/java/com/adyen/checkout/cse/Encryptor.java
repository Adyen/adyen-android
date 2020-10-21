/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 6/6/2019.
 */

package com.adyen.checkout.cse;

import androidx.annotation.NonNull;

import com.adyen.checkout.cse.internal.CardEncryptorImpl;

public final class Encryptor {

    @NonNull
    public static final CardEncryptor INSTANCE;

    static {
        INSTANCE = new CardEncryptorImpl();
    }

    private Encryptor() {
        throw new IllegalStateException("No instances.");
    }
}
