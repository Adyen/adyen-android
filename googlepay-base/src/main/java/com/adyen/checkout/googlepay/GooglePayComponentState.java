/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 7/8/2019.
 */

package com.adyen.checkout.googlepay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.PaymentComponentState;
import com.adyen.checkout.base.model.payments.request.GooglePayPaymentMethod;
import com.adyen.checkout.base.model.payments.request.PaymentComponentData;
import com.google.android.gms.wallet.PaymentData;

public class GooglePayComponentState extends PaymentComponentState<GooglePayPaymentMethod> {

    private final PaymentData mPaymentData;

    public GooglePayComponentState(
            @NonNull PaymentComponentData<GooglePayPaymentMethod> paymentComponentData,
            boolean isValid,
            @Nullable PaymentData paymentData) {
        super(paymentComponentData, isValid);
        mPaymentData = paymentData;
    }

    @Nullable
    public PaymentData getPaymentData() {
        return mPaymentData;
    }
}
