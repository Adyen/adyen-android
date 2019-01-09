/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 11/06/2018.
 */

package com.adyen.checkout.ui.internal.qiwiwallet;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.util.PaymentMethodTypes;

public final class QiwiWalletHandler implements PaymentMethodHandler {
    @NonNull
    public static final Factory FACTORY = new Factory() {
        @Override
        public boolean supports(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
            return PaymentMethodTypes.QIWIWALLET.equals(paymentMethod.getType());
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

    public QiwiWalletHandler(@NonNull PaymentReference paymentReference, @NonNull PaymentMethod paymentMethod) {
        mPaymentReference = paymentReference;
        mPaymentMethod = paymentMethod;
    }

    @Override
    public void handlePaymentMethodDetails(@NonNull Activity activity, int requestCode) {
        activity.finishActivity(requestCode);
        Intent intent = QiwiWalletPaymentDetailsActivity.newIntent(activity, mPaymentReference, mPaymentMethod);
        activity.startActivityForResult(intent, requestCode);
    }
}
