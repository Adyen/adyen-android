/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 11/06/2018.
 */

package com.adyen.checkout.ui.internal.def;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.PaymentHandler;
import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;

import java.util.List;

public final class DefaultPaymentMethodHandler implements PaymentMethodHandler {
    @NonNull
    public static final Factory FACTORY = new Factory() {
        @Override
        public boolean supports(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
            List<InputDetail> inputDetails = paymentMethod.getInputDetails();

            if (inputDetails != null) {
                for (InputDetail inputDetail : inputDetails) {
                    if (!inputDetail.isOptional()) {
                        return false;
                    }
                }
            }

            return true;
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

    private final PaymentReference mPaymentReference;

    private final PaymentMethod mPaymentMethod;

    public DefaultPaymentMethodHandler(@NonNull PaymentReference paymentReference, @NonNull PaymentMethod paymentMethod) {
        mPaymentReference = paymentReference;
        mPaymentMethod = paymentMethod;
    }

    @Override
    public void handlePaymentMethodDetails(@NonNull Activity activity, int requestCode) {
        PaymentHandler paymentHandler = mPaymentReference.getPaymentHandler(activity);
        paymentHandler.initiatePayment(mPaymentMethod, null);
    }
}
