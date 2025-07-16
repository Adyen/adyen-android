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
import kotlinx.coroutines.CoroutineScope

internal class AdvancedComponentEventHandler<T : BaseComponentState>(
    private val checkoutCallback: CheckoutCallback
) :
    ComponentEventHandler<T> {

    override fun initialize(coroutineScope: CoroutineScope) {
        // no ops
    }

    override fun onPaymentComponentEvent(event: PaymentComponentEvent<T>, onCheckoutResult: (CheckoutResult) -> Unit) {
        when (event) {
            is PaymentComponentEvent.Submit -> {
                checkoutCallback.beforeSubmit(event.state)
                checkoutCallback.onSubmit(event.state) { checkoutResult ->
                    onCheckoutResult(checkoutResult)
                }
            }
        }
    }

    override fun onCleared() {
        // no ops
    }
}
