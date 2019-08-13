/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/7/2019.
 */

package com.adyen.checkout.googlepay;

import android.support.annotation.Nullable;

import com.adyen.checkout.base.component.data.output.OutputData;
import com.adyen.checkout.core.util.StringUtil;
import com.adyen.checkout.googlepay.util.GooglePayUtils;
import com.google.android.gms.wallet.PaymentData;

public class GooglePayOutputData implements OutputData {

    private final PaymentData mPaymentData;

    GooglePayOutputData(PaymentData paymentData) {
        mPaymentData = paymentData;
    }

    @Override
    public boolean isValid() {
        return mPaymentData != null && StringUtil.hasContent(GooglePayUtils.findToken(mPaymentData));
    }

    @Nullable
    public PaymentData getPaymentData() {
        return mPaymentData;
    }
}
