package com.adyen.checkout.ui.internal.paypal;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.internal.model.PaymentMethodImpl;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethod;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethodFactory;
import com.adyen.checkout.util.PaymentMethodTypes;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 21/03/2018.
 */
public class PayPalCheckoutMethodFactory extends CheckoutMethodFactory {
    public PayPalCheckoutMethodFactory(@NonNull Application application) {
        super(application);
    }

    @Nullable
    @Override
    public Callable<List<CheckoutMethod>> initOneClickCheckoutMethods(@NonNull PaymentSession paymentSession) {
        final PaymentMethod paymentMethod = PaymentMethodImpl.findByType(paymentSession.getOneClickPaymentMethods(), PaymentMethodTypes.PAYPAL);
        final Application application = getApplication();

        if (paymentMethod != null
                && PayPalHandler.FACTORY.supports(application, paymentMethod)
                && PayPalHandler.FACTORY.isAvailableToShopper(application, paymentSession, paymentMethod)) {
            return new Callable<List<CheckoutMethod>>() {
                @Override
                public List<CheckoutMethod> call() {
                    return Collections.<CheckoutMethod>singletonList(new PayPalCheckoutMethod.OneClick(application, paymentMethod));
                }
            };
        }

        return null;
    }

    @Nullable
    @Override
    public Callable<List<CheckoutMethod>> initCheckoutMethods(@NonNull PaymentSession paymentSession) {
        final PaymentMethod paymentMethod = PaymentMethodImpl.findByType(paymentSession.getPaymentMethods(), PaymentMethodTypes.PAYPAL);
        final Application application = getApplication();

        if (paymentMethod != null
                && PayPalHandler.FACTORY.supports(application, paymentMethod)
                && PayPalHandler.FACTORY.isAvailableToShopper(application, paymentSession, paymentMethod)) {
            return new Callable<List<CheckoutMethod>>() {
                @Override
                public List<CheckoutMethod> call() {
                    return Collections.<CheckoutMethod>singletonList(new PayPalCheckoutMethod.Default(application, paymentMethod));
                }
            };
        }

        return null;
    }
}
