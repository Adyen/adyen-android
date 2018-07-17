package com.adyen.checkout.googlewallet;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.model.AndroidPayConfiguration;
import com.adyen.checkout.core.model.GooglePayConfiguration;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.googlewallet.internal.AndroidPayUtil;
import com.adyen.checkout.googlewallet.internal.GooglePayUtil;
import com.adyen.checkout.googlewallet.internal.GoogleWalletDetailsActivity;
import com.adyen.checkout.util.PaymentMethodTypes;

import java.util.concurrent.Callable;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 11/06/2018.
 */
public final class GoogleWalletHandler implements PaymentMethodHandler {
    public static final Factory FACTORY = new Factory() {
        @Override
        public boolean supports(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
            String paymentMethodType = paymentMethod.getType();

            return PaymentMethodTypes.ANDROID_PAY.equals(paymentMethodType)
                    || PaymentMethodTypes.GOOGLE_PAY.equals(paymentMethodType);
        }

        @Override
        public boolean isAvailableToShopper(
                @NonNull Application application,
                @NonNull PaymentSession paymentSession,
                @NonNull PaymentMethod paymentMethod
        ) {
            try {
                String paymentMethodType = paymentMethod.getType();

                if (PaymentMethodTypes.ANDROID_PAY.equals(paymentMethodType)) {
                    paymentMethod.getConfiguration(AndroidPayConfiguration.class);

                    return true;
                } else if (PaymentMethodTypes.GOOGLE_PAY.equals(paymentMethodType)) {
                    paymentMethod.getConfiguration(GooglePayConfiguration.class);

                    return true;
                }
            } catch (CheckoutException e) {
                // Invalid configuration.
            }

            return false;
        }
    };

    public static final String RESULT_ERROR_CODE = "RESULT_ERROR_CODE";

    private final PaymentReference mPaymentReference;

    private final PaymentMethod mPaymentMethod;

    /**
     * Returns a {@link Callable} that can be executed on a background thread to determine whether the shopper has setup the given payment method
     * and is ready to pay.
     *
     * @param application The {@link Application}.
     * @param paymentSession The {@link PaymentSession}.
     * @param paymentMethod The Google Wallet {@link PaymentMethod}.
     * @return A {@link Callable} to be executed on a background thread to determine whether the shopper has setup the given payment method and is
     * ready to pay.
     */
    @NonNull
    public static Callable<Boolean> getReadyToPayCallable(
            @NonNull Application application,
            @NonNull PaymentSession paymentSession,
            @NonNull PaymentMethod paymentMethod
    ) {
        if (PaymentMethodTypes.ANDROID_PAY.equals(paymentMethod.getType())) {
            return AndroidPayUtil.isReadyToPay(application, paymentSession, paymentMethod);
        } else  {
            return GooglePayUtil.isReadyToPay(application, paymentSession, paymentMethod);
        }
    }

    public GoogleWalletHandler(@NonNull PaymentReference paymentReference, @NonNull PaymentMethod paymentMethod) {
        mPaymentReference = paymentReference;
        mPaymentMethod = paymentMethod;
    }

    @Override
    public void handlePaymentMethodDetails(@NonNull Activity activity, int requestCode) {
        activity.finishActivity(requestCode);
        Intent intent = GoogleWalletDetailsActivity.newIntent(activity, mPaymentReference, mPaymentMethod);
        activity.startActivityForResult(intent, requestCode);
    }
}
