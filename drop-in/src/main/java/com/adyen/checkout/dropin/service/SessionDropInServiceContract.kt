/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/1/2023.
 */

package com.adyen.checkout.dropin.service

import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.Order
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails

// TODO SESSIONS: docs
interface SessionDropInServiceContract {
    fun onSubmit(state: PaymentComponentState<*>): Boolean = false
    fun onAdditionalDetails(actionComponentData: ActionComponentData): Boolean = false
    fun onBalanceCheck(paymentMethodDetails: PaymentMethodDetails): Boolean = false
    fun onOrderRequest(): Boolean = false
    fun onOrderCancel(order: Order, shouldUpdatePaymentMethods: Boolean): Boolean = false
}
