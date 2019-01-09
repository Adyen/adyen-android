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
import android.support.annotation.Nullable;

import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethod;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethodFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class OpenInvoiceCheckoutMethodFactory extends CheckoutMethodFactory {

    public OpenInvoiceCheckoutMethodFactory(@NonNull Application application) {
        super(application);
    }

    @Nullable
    @Override
    public Callable<List<CheckoutMethod>> initOneClickCheckoutMethods(@NonNull PaymentSession paymentSession) {
        return null;
    }

    @Nullable
    @Override
    public Callable<List<CheckoutMethod>> initCheckoutMethods(@NonNull PaymentSession paymentSession) {
        final List<CheckoutMethod> checkoutMethods = new ArrayList<>();

        for (PaymentMethod method : paymentSession.getPaymentMethods()) {
            if (OpenInvoiceHandler.FACTORY.supports(getApplication(), method)
                    && OpenInvoiceHandler.FACTORY.isAvailableToShopper(getApplication(), paymentSession, method)) {

                checkoutMethods.add(new OpenInvoiceCheckoutMethod(getApplication(), method));
            }
        }

        return new Callable<List<CheckoutMethod>>() {
            @Override
            public List<CheckoutMethod> call() {
                return checkoutMethods;
            }
        };
    }
}
