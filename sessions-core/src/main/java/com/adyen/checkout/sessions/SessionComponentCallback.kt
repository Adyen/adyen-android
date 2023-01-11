/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 10/1/2023.
 */

package com.adyen.checkout.sessions

import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.components.model.payments.response.Action

// BIG TODO SESSIONS: docs
interface SessionComponentCallback<T : PaymentComponentState<*>> {
    // Generic events
    fun onStateChanged(state: T): Boolean = false
    fun onAction(action: Action)
    fun onFinished(resultCode: String)
    fun onError(componentError: ComponentError)

    // API events
    fun onSubmit(state: T): Boolean = false
    fun onAdditionalDetails(actionComponentData: ActionComponentData): Boolean = false
    fun onBalanceCheck(paymentMethodDetails: PaymentMethodDetails): Boolean = false
    fun onOrderRequest(): Boolean = false
    fun onOrderCancel(orderRequest: OrderRequest): Boolean = false

    fun onLoading(isLoading: Boolean) = Unit
}
