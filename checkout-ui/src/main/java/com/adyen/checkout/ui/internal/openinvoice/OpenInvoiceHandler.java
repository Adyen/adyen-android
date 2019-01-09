/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/10/2018.
 */

package com.adyen.checkout.ui.internal.openinvoice;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.util.PaymentMethodTypes;

public class OpenInvoiceHandler implements PaymentMethodHandler {
    @NonNull
    public static final Factory FACTORY = new Factory() {

        @Override
        public boolean supports(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
            //fallback to DefaultPaymentMethod for HPP redirect if there are no input details
            return PaymentMethodTypes.KLARNA.equals(paymentMethod.getType())
                    && paymentMethod.getInputDetails() != null;
        }

        @Override
        public boolean isAvailableToShopper(@NonNull Application application, @NonNull PaymentSession paymentSession,
                                            @NonNull PaymentMethod paymentMethod) {
            return true;
        }
    };

    private final PaymentReference mPaymentReference;

    private final PaymentMethod mPaymentMethod;

    public OpenInvoiceHandler(@NonNull PaymentReference paymentReference, @NonNull PaymentMethod paymentMethod) {
        mPaymentReference = paymentReference;
        mPaymentMethod = paymentMethod;
    }


    @Override
    public void handlePaymentMethodDetails(@NonNull Activity activity, int requestCode) {
        Intent openInvoiceDetailsIntent = OpenInvoiceDetailsActivity.newIntent(activity, mPaymentReference, mPaymentMethod);
        activity.finishActivity(requestCode);
        activity.startActivity(openInvoiceDetailsIntent);
    }
}
