package com.adyen.checkout.ui.internal.common.model;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.PaymentHandler;
import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.ui.internal.common.fragment.CheckoutDetailsFragment;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 05/04/2018.
 */
public interface CheckoutHandler {
    @NonNull
    PaymentReference getPaymentReference();

    @NonNull
    PaymentHandler getPaymentHandler();

    void presentDetailsActivity(@NonNull Intent intent);

    void presentDetailsFragment(@NonNull CheckoutDetailsFragment fragment);

    void handleWithPaymentMethodHandler(@NonNull PaymentMethodHandler paymentMethodHandler);
}
