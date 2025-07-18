/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/6/2025.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.components.CheckoutCallback
import com.adyen.checkout.core.components.CheckoutResult

internal class AdvancedComponentEventHandler<T : BasePaymentComponentState>(
    private val checkoutCallback: CheckoutCallback
) : ComponentEventHandler<T> {

    override suspend fun onPaymentComponentEvent(event: PaymentComponentEvent<T>): CheckoutResult {
        return when (event) {
            is PaymentComponentEvent.Submit -> {
                checkoutCallback.beforeSubmit(event.state)
                checkoutCallback.onSubmit(event.state)
            }
        }
    }
}
