/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/6/2025.
 */

package com.adyen.checkout.core.internal

import com.adyen.checkout.core.CheckoutCallback
import com.adyen.checkout.core.paymentmethod.PaymentComponentState
import com.adyen.checkout.core.paymentmethod.PaymentMethodDetails
import kotlinx.coroutines.CoroutineScope

internal class AdvancedComponentEventHandler<T : PaymentComponentState<out PaymentMethodDetails>> :
    ComponentEventHandler<T> {
    override fun initialize(coroutineScope: CoroutineScope) {
        // no ops
    }

    override fun onCleared() {
        // no ops
    }

    override fun onPaymentComponentEvent(event: PaymentComponentEvent<T>, checkoutCallback: CheckoutCallback) {
        // TODO - Advanced Flow
    }
}
