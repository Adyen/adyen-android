/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/10/2018.
 */

package com.adyen.checkout.ui.internal.openinvoice;

import android.app.Application;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.ui.internal.common.model.CheckoutHandler;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethod;

public class OpenInvoiceCheckoutMethod extends CheckoutMethod {

    public OpenInvoiceCheckoutMethod(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
        super(application, paymentMethod);
    }

    @Override
    public void onSelected(@NonNull CheckoutHandler checkoutHandler) {
        PaymentReference paymentReference = checkoutHandler.getPaymentReference();
        PaymentMethodHandler paymentMethodHandler = new OpenInvoiceHandler(paymentReference, getPaymentMethod());
        checkoutHandler.handleWithPaymentMethodHandler(paymentMethodHandler);
    }
}
