/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */

package com.adyen.checkout.components;

import androidx.annotation.NonNull;

import com.adyen.checkout.components.model.payments.request.PaymentComponentData;
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails;

/**
 * The current state of a PaymentComponent.
 */
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class PaymentComponentState<PaymentMethodDetailsT extends PaymentMethodDetails> {

    private final PaymentComponentData<PaymentMethodDetailsT> mPaymentComponentData;
    private final boolean mIsInputValid;
    private final boolean mIsReady;

    public PaymentComponentState(@NonNull PaymentComponentData<PaymentMethodDetailsT> paymentComponentData, boolean isInputValid, boolean isReady) {
        mPaymentComponentData = paymentComponentData;
        mIsInputValid = isInputValid;
        mIsReady = isReady;
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
        return mIsInputValid && mIsReady;
    }

    /**
     * @return If the component UI data is valid.
     */
    public boolean isInputValid() {
        return mIsInputValid;
    }

    /**
     * @return If the component initialisation is done and data can be sent to the backend when valid.
     */
    public boolean isReady() {
        return mIsReady;
    }
}
