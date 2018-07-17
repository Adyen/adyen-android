package com.adyen.checkout.ui.internal;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.core.StartPaymentParameters;
import com.adyen.checkout.ui.internal.common.activity.CheckoutActivity;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 02/07/2018.
 */
public final class CheckoutHandler implements PaymentMethodHandler {
    private final StartPaymentParameters mPaymentParameters;

    public CheckoutHandler(@NonNull StartPaymentParameters paymentParameters) {
        mPaymentParameters = paymentParameters;
    }

    @Override
    public void handlePaymentMethodDetails(@NonNull Activity activity, int requestCode) {
        activity.finishActivity(requestCode);
        Intent checkoutIntent = CheckoutActivity.newIntent(activity, mPaymentParameters.getPaymentReference());
        activity.startActivityForResult(checkoutIntent, requestCode);
        activity.overridePendingTransition(0, 0);
    }
}
