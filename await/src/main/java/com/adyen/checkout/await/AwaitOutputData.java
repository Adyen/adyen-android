/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/8/2020.
 */

package com.adyen.checkout.await;

import androidx.annotation.Nullable;

import com.adyen.checkout.base.component.OutputData;

class AwaitOutputData implements OutputData {

    private final boolean mIsValid;
    private final String mPaymentMethodType;

    AwaitOutputData(boolean isValid, @Nullable String paymentMethodType) {
        mIsValid = isValid;
        mPaymentMethodType = paymentMethodType;
    }

    @Override
    public boolean isValid() {
        return mIsValid;
    }

    @Nullable
    public String getPaymentMethodType() {
        return mPaymentMethodType;
    }
}
