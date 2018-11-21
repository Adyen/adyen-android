package com.adyen.checkout.ui.internal.googlewallet;

import android.app.Application;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.googlepay.GooglePayHandler;
import com.adyen.checkout.ui.internal.common.model.CheckoutHandler;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethod;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 10/04/2018.
 */
final class GooglePayCheckoutMethod extends CheckoutMethod {
    GooglePayCheckoutMethod(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
        super(application, paymentMethod);
    }

    @Override
    public void onSelected(@NonNull CheckoutHandler checkoutHandler) {
        PaymentReference paymentReference = checkoutHandler.getPaymentReference();
        PaymentMethodHandler paymentMethodHandler = new GooglePayHandler(paymentReference, getPaymentMethod());
        checkoutHandler.handleWithPaymentMethodHandler(paymentMethodHandler);
    }
}
