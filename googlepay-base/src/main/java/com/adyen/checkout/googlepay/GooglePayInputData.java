/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/7/2019.
 */

package com.adyen.checkout.googlepay;

import androidx.annotation.Nullable;

import com.adyen.checkout.base.component.InputData;
import com.google.android.gms.wallet.PaymentData;

class GooglePayInputData implements InputData {

    private PaymentData mPaymentData;

    @Nullable
    public PaymentData getPaymentData() {
        return mPaymentData;
    }

    public void setPaymentData(@Nullable PaymentData paymentData) {
        mPaymentData = paymentData;
    }
}
