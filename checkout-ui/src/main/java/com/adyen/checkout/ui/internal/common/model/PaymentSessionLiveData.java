/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 29/03/2018.
 */

package com.adyen.checkout.ui.internal.common.model;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.model.PaymentSession;

public class PaymentSessionLiveData extends LiveData<PaymentSession> {
    PaymentSessionLiveData(@NonNull PaymentSession paymentSession) {
        setValue(paymentSession);
    }

    @NonNull
    @Override
    public PaymentSession getValue() {
        //noinspection ConstantConditions, is set at beginning.
        return super.getValue();
    }

    @Override
    protected void postValue(@NonNull PaymentSession value) {
        super.postValue(value);
    }

    @Override
    protected void setValue(@NonNull PaymentSession value) {
        super.setValue(value);
    }
}
