/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/6/2025.
 */

package com.adyen.checkout.core.internal

import com.adyen.checkout.core.CheckoutCallback
import com.adyen.checkout.core.CheckoutResult
import kotlinx.coroutines.CoroutineScope

internal class AdvancedComponentEventHandler<T : BaseComponentState>(
    private val checkoutCallback: CheckoutCallback
) :
    ComponentEventHandler<T> {

    override fun initialize(coroutineScope: CoroutineScope) {
        // no ops
    }

    override fun onPaymentComponentEvent(event: PaymentComponentEvent<T>) {
        when (event) {
            is PaymentComponentEvent.Submit -> {
                checkoutCallback.beforeSubmit(event.state)
                checkoutCallback.onSubmit(event.state) { checkoutResult ->
                    when (checkoutResult) {
                        is CheckoutResult.Action -> {
                            // TODO - Handle Action
                        }
                        is CheckoutResult.Error -> {
                            // TODO - Handle Error
                        }
                        is CheckoutResult.Finished -> {
                            // TODO - Handle Finished
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        // no ops
    }
}
