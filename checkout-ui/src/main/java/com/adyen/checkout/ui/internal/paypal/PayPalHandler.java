package com.adyen.checkout.ui.internal.paypal;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.PaymentHandler;
import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.internal.model.InputDetailImpl;
import com.adyen.checkout.core.model.PayPalDetails;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.util.PaymentMethodTypes;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 11/06/2018.
 */
public final class PayPalHandler implements PaymentMethodHandler {
    public static final Factory FACTORY = new Factory() {
        @Override
        public boolean supports(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
            return PaymentMethodTypes.PAYPAL.equals(paymentMethod.getType());
        }

        @Override
        public boolean isAvailableToShopper(
                @NonNull Application application,
                @NonNull PaymentSession paymentSession,
                @NonNull PaymentMethod paymentMethod
        ) {
            return true;
        }
    };

    private static final Boolean STORE_DETAILS = null;

    private final PaymentReference mPaymentReference;

    private final PaymentMethod mPaymentMethod;

    public PayPalHandler(@NonNull PaymentReference paymentReference, @NonNull PaymentMethod paymentMethod) {
        mPaymentReference = paymentReference;
        mPaymentMethod = paymentMethod;
    }

    @Override
    public void handlePaymentMethodDetails(@NonNull Activity activity, int requestCode) {
        PayPalDetails payPalDetails;

        if (InputDetailImpl.findByKey(mPaymentMethod.getInputDetails(), PayPalDetails.KEY_STORE_DETAILS) != null) {
            payPalDetails = new PayPalDetails.Builder().setStoreDetails(STORE_DETAILS).build();
        } else {
            payPalDetails = null;
        }

        PaymentHandler paymentHandler = mPaymentReference.getPaymentHandler(activity);
        paymentHandler.initiatePayment(mPaymentMethod, payPalDetails);
    }
}
