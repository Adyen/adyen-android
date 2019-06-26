/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */

package com.adyen.checkout.base;

import android.support.annotation.NonNull;

import com.adyen.checkout.base.model.payments.request.PaymentComponentData;

/**
 * The current state of a PaymentComponent.
 *
 * @param <PaymentComponentDataT> The collected data that can used to fill the payments/ API call.
 */
public final class PaymentComponentState<PaymentComponentDataT extends PaymentComponentData> {

    private final PaymentComponentDataT mPaymentMethodForRequest;
    private final boolean mIsValid;

    public PaymentComponentState(@NonNull PaymentComponentDataT paymentMethodParam, boolean isValid) {
        mPaymentMethodForRequest = paymentMethodParam;
        mIsValid = isValid;
    }

    /**
     * @return The data that was collected by the component.
     */
    @NonNull
    public PaymentComponentDataT getData() {
        return mPaymentMethodForRequest;
    }

    /**
     * @return If the collected data is valid to be sent to the backend.
     */
    public boolean isValid() {
        return mIsValid;
    }
}
