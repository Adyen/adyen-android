/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.internal

import com.adyen.checkout.core.paymentmethod.PaymentComponentState

internal class DefaultComponentEventHandler<T : PaymentComponentState<*>> : ComponentEventHandler<T> {

    override fun onPaymentComponentEvent(event: PaymentComponentEvent<T>) {
        when (event) {
            is PaymentComponentEvent.Submit -> {
                // submit
            }
        }
    }
}
