/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 20/03/2018.
 */

package com.adyen.checkout.ui.internal.googlepay;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.adyen.checkout.core.internal.model.PaymentMethodImpl;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.googlepay.GooglePayHandler;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethod;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethodFactory;
import com.adyen.checkout.util.PaymentMethodTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class GooglePayCheckoutMethodFactory extends CheckoutMethodFactory {
    public GooglePayCheckoutMethodFactory(@NonNull Application application) {
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
        return new GooglePayCallable(getApplication(), paymentSession);
    }

    private static final class GooglePayCallable implements Callable<List<CheckoutMethod>> {
        private static final String TAG = GooglePayCallable.class.getSimpleName();

        private static final String ERROR_MESSAGE_FORMAT = "PaymentMethod with type '%s' is present, but the 'checkout-googlepay' dependency is "
                + "missing. Add the dependency to your dependency list.";

        private final Application mApplication;

        private final PaymentSession mPaymentSession;

        private GooglePayCallable(@NonNull Application application, @NonNull PaymentSession paymentSession) {
            mApplication = application;
            mPaymentSession = paymentSession;
        }

        @Override
        public List<CheckoutMethod> call() {
            List<CheckoutMethod> checkoutMethods = new ArrayList<>();
            List<PaymentMethod> paymentMethods = mPaymentSession.getPaymentMethods();
            PaymentMethod googlePayPaymentMethod = PaymentMethodImpl.findByType(paymentMethods, PaymentMethodTypes.GOOGLE_PAY);

            if (googlePayPaymentMethod != null) {
                try {
                    if (GooglePayHandler.FACTORY.supports(mApplication, googlePayPaymentMethod)
                            && GooglePayHandler.FACTORY.isAvailableToShopper(mApplication, mPaymentSession, googlePayPaymentMethod)
                            && GooglePayHandler.getReadyToPayCallable(mApplication, mPaymentSession, googlePayPaymentMethod).call()) {
                        checkoutMethods.add(new GooglePayCheckoutMethod(mApplication, googlePayPaymentMethod));
                    }
                } catch (NoClassDefFoundError e) {
                    Log.e(TAG, String.format(ERROR_MESSAGE_FORMAT, googlePayPaymentMethod.getType()));
                } catch (Exception e) {
                    Log.w(TAG, e);
                }
            }

            return checkoutMethods;
        }
    }
}
