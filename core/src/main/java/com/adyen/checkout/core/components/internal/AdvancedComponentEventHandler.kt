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

internal class AdvancedComponentEventHandler<T : BasePaymentComponentState>(
    private val componentCallbacks: AdvancedComponentCallbacks,
) : ComponentEventHandler<T> {

    override suspend fun onPaymentComponentEvent(event: PaymentComponentEvent<T>): CheckoutResult {
        return when (event) {
            is PaymentComponentEvent.Submit -> {
                componentCallbacks.beforeSubmit(event.state)
                val result = componentCallbacks.onSubmit(event.state)
                if (result is CheckoutResult.Finished) {
                    componentCallbacks.onFinished(result.paymentResult)
                }
                result
            }

            is PaymentComponentEvent.Error -> {
                componentCallbacks.onError(event.error)
                CheckoutResult.Error(event.error)
            }
        }
    }

    override suspend fun onActionComponentEvent(event: ActionComponentEvent): CheckoutResult {
        return when (event) {
            is ActionComponentEvent.ActionDetails -> {
                val result = componentCallbacks.onAdditionalDetails(event.data)
                if (result is CheckoutResult.Finished) {
                    componentCallbacks.onFinished(result.paymentResult)
                }
                result
            }

            is ActionComponentEvent.Error -> {
                componentCallbacks.onError(event.error)
                CheckoutResult.Error(event.error)
            }
        }
    }
}
