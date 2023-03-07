/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 10/1/2023.
 */

package com.adyen.checkout.sessions.core

import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.internal.BaseComponentCallback
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.components.core.action.Action

// BIG TODO SESSIONS: docs
interface SessionComponentCallback<T : PaymentComponentState<*>> : BaseComponentCallback {
    // Generic events
    fun onStateChanged(state: T) = Unit
    fun onAction(action: Action)
    fun onFinished(result: SessionPaymentResult)
    fun onError(componentError: ComponentError)

    // API events
    fun onSubmit(state: T): Boolean = false
    fun onAdditionalDetails(actionComponentData: ActionComponentData): Boolean = false
    fun onBalanceCheck(paymentMethodDetails: PaymentMethodDetails): Boolean = false
    fun onOrderRequest(): Boolean = false
    fun onOrderCancel(order: Order): Boolean = false

    fun onLoading(isLoading: Boolean) = Unit
}
