/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/7/2019.
 */

package com.adyen.checkout.googlepay;

import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.adyen.checkout.base.component.OutputData;
import com.adyen.checkout.googlepay.util.GooglePayUtils;
import com.google.android.gms.wallet.PaymentData;

class GooglePayOutputData implements OutputData {

    private final PaymentData mPaymentData;

    GooglePayOutputData(PaymentData paymentData) {
        mPaymentData = paymentData;
    }

    @Override
    public boolean isValid() {
        return mPaymentData != null && !TextUtils.isEmpty(GooglePayUtils.findToken(mPaymentData));
    }

    @Nullable
    public PaymentData getPaymentData() {
        return mPaymentData;
    }
}
