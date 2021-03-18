/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/1/2021.
 */

package com.adyen.checkout.components;

import androidx.annotation.NonNull;

import com.adyen.checkout.components.model.payments.request.PaymentComponentData;
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails;

public class GenericComponentState<PaymentMethodDetailsT extends PaymentMethodDetails> extends PaymentComponentState<PaymentMethodDetailsT> {

    /**
     * PaymentComponentState for any component without additional data.
     */
    public GenericComponentState(
            @NonNull PaymentComponentData<PaymentMethodDetailsT> paymentComponentData,
            boolean isInputValid,
            boolean isReady
    ) {
        super(paymentComponentData, isInputValid, isReady);
    }
}
