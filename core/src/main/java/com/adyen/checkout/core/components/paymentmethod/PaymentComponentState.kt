/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.components.paymentmethod

import com.adyen.checkout.core.components.data.PaymentComponentData

/**
 * The current state of a PaymentComponent.
 */
interface PaymentComponentState<PaymentMethodDetailsT : PaymentMethodDetails> {

    /**
     * @return The data that was collected by the component.
     */
    val data: PaymentComponentData<PaymentMethodDetailsT>

    /**
     * @return If the component UI data is valid.
     */
    val isValid: Boolean
}
