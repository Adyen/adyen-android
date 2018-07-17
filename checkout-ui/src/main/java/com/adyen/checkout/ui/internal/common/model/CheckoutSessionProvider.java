package com.adyen.checkout.ui.internal.common.model;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.PaymentHandler;
import com.adyen.checkout.core.PaymentReference;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 02/05/2018.
 */
public interface CheckoutSessionProvider {
    @NonNull
    PaymentReference getPaymentReference();

    @NonNull
    PaymentHandler getPaymentHandler();
}
