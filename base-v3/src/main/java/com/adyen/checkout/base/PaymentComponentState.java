/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */

package com.adyen.checkout.base;

import androidx.annotation.NonNull;

import com.adyen.checkout.base.model.payments.request.PaymentComponentData;
import com.adyen.checkout.base.model.payments.request.PaymentMethodDetails;

/**
 * The current state of a PaymentComponent.
 */
public class PaymentComponentState<PaymentMethodDetailsT extends PaymentMethodDetails> {

    private final PaymentComponentData<PaymentMethodDetailsT> mPaymentComponentData;
    private final boolean mIsValid;

    public PaymentComponentState(@NonNull PaymentComponentData<PaymentMethodDetailsT> paymentComponentData, boolean isValid) {
        mPaymentComponentData = paymentComponentData;
        mIsValid = isValid;
    }

    /**
     * @return The data that was collected by the component.
     */
    @NonNull
    public PaymentComponentData<PaymentMethodDetailsT> getData() {
        return mPaymentComponentData;
    }

    /**
     * @return If the collected data is valid to be sent to the backend.
     */
    public boolean isValid() {
        return mIsValid;
    }
}
