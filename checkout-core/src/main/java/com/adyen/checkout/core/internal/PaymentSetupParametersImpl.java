package com.adyen.checkout.core.internal;

import android.content.Context;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.PaymentSetupParameters;
import com.adyen.checkout.core.internal.model.DeviceFingerprint;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 11/07/2018.
 */
public final class PaymentSetupParametersImpl implements PaymentSetupParameters {
    private final String mSdkToken;

    public PaymentSetupParametersImpl(@NonNull Context context) throws CheckoutException {
        mSdkToken = DeviceFingerprint.generateSdkToken(context, "custom");
    }

    @NonNull
    @Override
    public String getSdkToken() {
        return mSdkToken;
    }
}
