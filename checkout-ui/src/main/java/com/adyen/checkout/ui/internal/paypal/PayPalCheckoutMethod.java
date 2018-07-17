package com.adyen.checkout.ui.internal.paypal;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.StoredDetails;
import com.adyen.checkout.ui.internal.common.model.CheckoutHandler;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethod;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 10/04/2018.
 */
abstract class PayPalCheckoutMethod extends CheckoutMethod {
    private PayPalCheckoutMethod(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
        super(application, paymentMethod);
    }

    @Override
    public void onSelected(@NonNull CheckoutHandler checkoutHandler) {
        PaymentReference paymentReference = checkoutHandler.getPaymentReference();
        PaymentMethodHandler paymentMethodHandler = new PayPalHandler(paymentReference, getPaymentMethod());
        checkoutHandler.handleWithPaymentMethodHandler(paymentMethodHandler);
    }

    static final class Default extends PayPalCheckoutMethod {
        Default(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
            super(application, paymentMethod);
        }
    }

    static final class OneClick extends PayPalCheckoutMethod {
        OneClick(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
            super(application, paymentMethod);
        }

        @Nullable
        @Override
        public String getSecondaryText() {
            StoredDetails storedDetails = getPaymentMethod().getStoredDetails();
            String emailAddress = storedDetails != null ? storedDetails.getEmailAddress() : null;

            if (emailAddress != null) {
                return emailAddress;
            } else {
                return super.getSecondaryText();
            }
        }
    }
}
