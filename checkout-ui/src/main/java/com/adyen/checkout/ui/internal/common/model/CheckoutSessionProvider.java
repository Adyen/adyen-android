/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 02/05/2018.
 */

package com.adyen.checkout.ui.internal.common.model;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.PaymentHandler;
import com.adyen.checkout.core.PaymentReference;

public interface CheckoutSessionProvider {
    @NonNull
    PaymentReference getPaymentReference();

    @NonNull
    PaymentHandler getPaymentHandler();
}
