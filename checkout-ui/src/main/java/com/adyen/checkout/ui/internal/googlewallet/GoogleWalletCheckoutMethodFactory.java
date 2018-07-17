package com.adyen.checkout.ui.internal.googlewallet;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.adyen.checkout.core.internal.model.PaymentMethodImpl;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.googlewallet.GoogleWalletHandler;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethod;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethodFactory;
import com.adyen.checkout.util.PaymentMethodTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 20/03/2018.
 */
public class GoogleWalletCheckoutMethodFactory extends CheckoutMethodFactory {
    public GoogleWalletCheckoutMethodFactory(@NonNull Application application) {
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
        return new GoogleWalletCallable(getApplication(), paymentSession);
    }

    private static final class GoogleWalletCallable implements Callable<List<CheckoutMethod>> {
        private static final String TAG = GoogleWalletCallable.class.getSimpleName();

        private static final String ERROR_MESSAGE_FORMAT = "PaymentMethod with type '%s' is present, but the 'checkout-googlewallet' dependency is "
                + "missing. Add the dependency to your dependency list.";

        private final Application mApplication;

        private final PaymentSession mPaymentSession;

        private GoogleWalletCallable(@NonNull Application application, @NonNull PaymentSession paymentSession) {
            mApplication = application;
            mPaymentSession = paymentSession;
        }

        @Override
        public List<CheckoutMethod> call() {
            List<CheckoutMethod> checkoutMethods = new ArrayList<>();
            List<PaymentMethod> paymentMethods = mPaymentSession.getPaymentMethods();

            PaymentMethod androidPayPaymentMethod = PaymentMethodImpl.findByType(paymentMethods, PaymentMethodTypes.ANDROID_PAY);

            if (androidPayPaymentMethod != null) {
                try {
                    if (GoogleWalletHandler.FACTORY.supports(mApplication, androidPayPaymentMethod)
                            && GoogleWalletHandler.FACTORY.isAvailableToShopper(mApplication, mPaymentSession, androidPayPaymentMethod)
                            && GoogleWalletHandler.getReadyToPayCallable(mApplication, mPaymentSession, androidPayPaymentMethod).call()) {
                        checkoutMethods.add(new GoogleWalletCheckoutMethod.AndroidPay(mApplication, androidPayPaymentMethod));
                    }
                } catch (NoClassDefFoundError e) {
                    Log.e(TAG, String.format(ERROR_MESSAGE_FORMAT, androidPayPaymentMethod.getType()));
                } catch (Exception e) {
                    Log.w(TAG, e);
                }
            }

            PaymentMethod googlePayPaymentMethod = PaymentMethodImpl.findByType(paymentMethods, PaymentMethodTypes.GOOGLE_PAY);

            if (googlePayPaymentMethod != null) {
                try {
                    if (GoogleWalletHandler.FACTORY.supports(mApplication, googlePayPaymentMethod)
                            && GoogleWalletHandler.FACTORY.isAvailableToShopper(mApplication, mPaymentSession, googlePayPaymentMethod)
                            && GoogleWalletHandler.getReadyToPayCallable(mApplication, mPaymentSession, googlePayPaymentMethod).call()) {
                        checkoutMethods.add(new GoogleWalletCheckoutMethod.GooglePay(mApplication, googlePayPaymentMethod));
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
