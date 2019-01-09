/*
 * Copyright (c) 2018 Adyen B.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 11/06/2018.
 */

package com.adyen.checkout.googlepay;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.model.GooglePayConfiguration;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.googlepay.internal.GooglePayDetailsActivity;
import com.adyen.checkout.googlepay.internal.IsReadyToPayCallable;
import com.adyen.checkout.util.PaymentMethodTypes;

import java.util.concurrent.Callable;

public final class GooglePayHandler implements PaymentMethodHandler {
    @NonNull
    public static final Factory FACTORY = new Factory() {
        @Override
        public boolean supports(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
            String paymentMethodType = paymentMethod.getType();

            return PaymentMethodTypes.GOOGLE_PAY.equals(paymentMethodType);
        }

        @Override
        public boolean isAvailableToShopper(
                @NonNull Application application,
                @NonNull PaymentSession paymentSession,
                @NonNull PaymentMethod paymentMethod
        ) {
            try {
                paymentMethod.getConfiguration(GooglePayConfiguration.class);

                return true;
            } catch (CheckoutException e) {
                return false;
            }
        }
    };

    @NonNull
    public static final String RESULT_ERROR_CODE = "RESULT_ERROR_CODE";

    private final PaymentReference mPaymentReference;

    private final PaymentMethod mPaymentMethod;

    /**
     * Returns a {@link Callable} that can be executed on a background thread to determine whether the shopper has setup Google Pay and is ready to
     * pay.
     *
     * @param application The {@link Application}.
     * @param paymentSession The {@link PaymentSession}.
     * @param paymentMethod The Google Pay {@link PaymentMethod}.
     * @return A {@link Callable} that can be executed on a background thread to determine whether the shopper has setup Google Pay and is ready to
     *         pay.
     */
    @NonNull
    public static Callable<Boolean> getReadyToPayCallable(
            @NonNull Application application,
            @NonNull PaymentSession paymentSession,
            @NonNull PaymentMethod paymentMethod
    ) {
        return new IsReadyToPayCallable(application, paymentSession, paymentMethod);
    }

    public GooglePayHandler(@NonNull PaymentReference paymentReference, @NonNull PaymentMethod paymentMethod) {
        mPaymentReference = paymentReference;
        mPaymentMethod = paymentMethod;
    }

    @Override
    public void handlePaymentMethodDetails(@NonNull Activity activity, int requestCode) {
        activity.finishActivity(requestCode);
        Intent intent = GooglePayDetailsActivity.newIntent(activity, mPaymentReference, mPaymentMethod);
        activity.startActivityForResult(intent, requestCode);
    }
}
