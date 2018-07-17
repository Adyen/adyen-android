package com.adyen.checkout.core.internal;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.StartPaymentParameters;
import com.adyen.checkout.core.internal.model.PaymentSessionImpl;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 11/07/2018.
 */
public final class StartPaymentParametersImpl implements StartPaymentParameters {
    private final PaymentReferenceImpl mPaymentReference;

    private final PaymentSessionImpl mPaymentSession;

    public StartPaymentParametersImpl(@NonNull PaymentReferenceImpl paymentReference, @NonNull PaymentSessionImpl paymentSession) {
        mPaymentReference = paymentReference;
        mPaymentSession = paymentSession;
    }

    @NonNull
    @Override
    public PaymentReferenceImpl getPaymentReference() {
        return mPaymentReference;
    }

    @NonNull
    @Override
    public PaymentSessionImpl getPaymentSession() {
        return mPaymentSession;
    }
}
