package com.adyen.checkout.ui.internal.qiwiwallet;

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
 * Created by timon on 03/04/2018.
 */
public class QiwiWalletCheckoutMethodFactory extends CheckoutMethodFactory {
    public QiwiWalletCheckoutMethodFactory(@NonNull Application application) {
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
        final PaymentMethod paymentMethod = PaymentMethodImpl.findByType(paymentSession.getPaymentMethods(), PaymentMethodTypes.QIWIWALLET);
        final Application application = getApplication();

        if (paymentMethod != null
                && QiwiWalletHandler.FACTORY.supports(application, paymentMethod)
                && QiwiWalletHandler.FACTORY.isAvailableToShopper(application, paymentSession, paymentMethod)) {
            return new Callable<List<CheckoutMethod>>() {
                @Override
                public List<CheckoutMethod> call() {
                    return Collections.<CheckoutMethod>singletonList(new QiwiWalletCheckoutMethod(application, paymentMethod));
                }
            };
        }

        return null;
    }
}
