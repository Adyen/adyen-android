/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 20/03/2018.
 */

package com.adyen.checkout.ui.internal.def;

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

public class DefaultCheckoutMethodFactory extends CheckoutMethodFactory {
    private final List<PaymentMethod> mHandledOneClickPaymentMethods;

    private final List<PaymentMethod> mHandledPaymentMethods;

    public DefaultCheckoutMethodFactory(@NonNull Application application) {
        super(application);

        mHandledOneClickPaymentMethods = new ArrayList<>();
        mHandledPaymentMethods = new ArrayList<>();
    }

    @Nullable
    @Override
    public Callable<List<CheckoutMethod>> initOneClickCheckoutMethods(@NonNull PaymentSession paymentSession) {
        final List<CheckoutMethod> checkoutMethods = new ArrayList<>();
        List<PaymentMethod> oneClickPaymentMethods = paymentSession.getOneClickPaymentMethods();

        if (oneClickPaymentMethods != null) {
            Application application = getApplication();

            for (PaymentMethod oneClickPaymentMethod : oneClickPaymentMethods) {
                if (!mHandledOneClickPaymentMethods.contains(oneClickPaymentMethod)
                        && DefaultPaymentMethodHandler.FACTORY.supports(application, oneClickPaymentMethod)
                        && DefaultPaymentMethodHandler.FACTORY.isAvailableToShopper(application, paymentSession, oneClickPaymentMethod)) {
                    checkoutMethods.add(new DefaultCheckoutMethod(application, oneClickPaymentMethod));
                }
            }
        }

        return new Callable<List<CheckoutMethod>>() {
            @Override
            public List<CheckoutMethod> call() {
                return checkoutMethods;
            }
        };
    }

    @Nullable
    @Override
    public Callable<List<CheckoutMethod>> initCheckoutMethods(@NonNull PaymentSession paymentSession) {
        final List<CheckoutMethod> checkoutMethods = new ArrayList<>();
        List<PaymentMethod> paymentMethods = paymentSession.getPaymentMethods();

        Application application = getApplication();

        for (PaymentMethod paymentMethod : paymentMethods) {
            if (!mHandledPaymentMethods.contains(paymentMethod)
                    && DefaultPaymentMethodHandler.FACTORY.supports(application, paymentMethod)
                    && DefaultPaymentMethodHandler.FACTORY.isAvailableToShopper(application, paymentSession, paymentMethod)) {
                checkoutMethods.add(new DefaultCheckoutMethod(application, paymentMethod));
            }
        }

        return new Callable<List<CheckoutMethod>>() {
            @Override
            public List<CheckoutMethod> call() throws Exception {
                return checkoutMethods;
            }
        };
    }

    public void setHandledOneClickPaymentMethods(@Nullable List<PaymentMethod> handledOneClickPaymentMethods) {
        mHandledOneClickPaymentMethods.clear();

        if (handledOneClickPaymentMethods != null) {
            mHandledOneClickPaymentMethods.addAll(handledOneClickPaymentMethods);
        }
    }

    public void setHandledPaymentMethods(@Nullable List<PaymentMethod> handledPaymentMethods) {
        mHandledPaymentMethods.clear();

        if (handledPaymentMethods != null) {
            mHandledPaymentMethods.addAll(handledPaymentMethods);
        }
    }
}
