/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/6/2025.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.error.toCheckoutError

internal class AdvancedComponentEventHandler<T : BasePaymentComponentState>(
    private val componentCallbacks: AdvancedComponentCallbacks,
) : ComponentEventHandler<T> {

    override suspend fun onPaymentComponentEvent(event: PaymentComponentEvent<T>): CheckoutResult {
        return when (event) {
            is PaymentComponentEvent.Submit -> {
                componentCallbacks.beforeSubmit(event.state)
                componentCallbacks.onSubmit(event.state)
            }

            is PaymentComponentEvent.Error -> {
                componentCallbacks.onError(event.error.toCheckoutError())
                CheckoutResult.Error(event.error.message.orEmpty())
            }
        }
    }

    override suspend fun onActionComponentEvent(event: ActionComponentEvent): CheckoutResult {
        return when (event) {
            is ActionComponentEvent.ActionDetails -> componentCallbacks.onAdditionalDetails(event.data)
            is ActionComponentEvent.Error -> {
                componentCallbacks.onError(event.error.toCheckoutError())
                CheckoutResult.Error(event.error.message.orEmpty())
            }
        }
    }
}
