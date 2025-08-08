/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/6/2025.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutResult

internal class AdvancedComponentEventHandler<T : BasePaymentComponentState>(
    private val checkoutCallbacks: CheckoutCallbacks
) : ComponentEventHandler<T> {

    override suspend fun onPaymentComponentEvent(event: PaymentComponentEvent<T>): CheckoutResult {
        return when (event) {
            is PaymentComponentEvent.Submit -> {
                checkoutCallbacks.beforeSubmit(event.state)
                checkoutCallbacks.onSubmit(event.state)
            }
        }
    }

    override suspend fun onActionComponentEvent(event: ActionComponentEvent): CheckoutResult {
        return when (event) {
            is ActionComponentEvent.ActionDetails -> checkoutCallbacks.onAdditionalDetails(event.data)
            is ActionComponentEvent.Error -> {
                checkoutCallbacks.onError(event.error)
                CheckoutResult.Error(event.error)
            }
        }
    }
}
