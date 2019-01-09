/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 21/06/2018.
 */

package com.adyen.checkout.core;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.model.PaymentSession;

/**
 * The {@link StartPaymentParameters} interface provides all objects that are required to start the actual payment.
 */
public interface StartPaymentParameters {
    /**
     * @return The current {@link PaymentReference}.
     */
    @NonNull
    PaymentReference getPaymentReference();

    /**
     * @return The current {@link PaymentSession}.
     */
    @NonNull
    PaymentSession getPaymentSession();
}
