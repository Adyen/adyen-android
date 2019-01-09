/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 11/07/2018.
 */

package com.adyen.checkout.core.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.PaymentReference;

import java.util.HashMap;
import java.util.Map;

public final class PaymentHandlerStore {
    private static PaymentHandlerStore sInstance = new PaymentHandlerStore();

    private final Map<String, PaymentHandlerImpl> mPaymentHandlers;

    @NonNull
    public static PaymentHandlerStore getInstance() {
        return sInstance;
    }

    private PaymentHandlerStore() {
        mPaymentHandlers = new HashMap<>();
    }

    @Nullable
    public PaymentHandlerImpl getPaymentHandler(@NonNull PaymentReference paymentReference) {
        return mPaymentHandlers.get(paymentReference.getUuid());
    }

    public void storePaymentHandler(@NonNull PaymentReference paymentReference, @NonNull PaymentHandlerImpl paymentHandler) {
        mPaymentHandlers.put(paymentReference.getUuid(), paymentHandler);
    }
}
