/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin

import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.dropin.internal.service.BaseDropInService

/**
 * Extend this service to interact with Drop-in and make the required network calls to the Adyen Checkout APIs through
 * your backend.
 *
 * Make sure you add your implementation of this service in your manifest file.
 *
 * You need to implement the [onSubmit] and [onAdditionalDetails] with this service. The rest of the methods are
 * optional.
 */
abstract class DropInService : BaseDropInService(), DropInServiceContract {

    final override fun requestPaymentsCall(paymentComponentState: PaymentComponentState<*>) {
        adyenLog(AdyenLogLevel.DEBUG) { "requestPaymentsCall" }
        onSubmit(paymentComponentState)
    }

    final override fun requestDetailsCall(actionComponentData: ActionComponentData) {
        adyenLog(AdyenLogLevel.DEBUG) { "requestDetailsCall" }
        onAdditionalDetails(actionComponentData)
    }

    final override fun requestBalanceCall(paymentComponentState: PaymentComponentState<*>) {
        adyenLog(AdyenLogLevel.DEBUG) { "requestBalanceCall" }
        onBalanceCheck(paymentComponentState)
    }

    final override fun requestOrdersCall() {
        adyenLog(AdyenLogLevel.DEBUG) { "requestOrdersCall" }
        onOrderRequest()
    }

    final override fun requestCancelOrder(order: OrderRequest, isDropInCancelledByUser: Boolean) {
        adyenLog(AdyenLogLevel.DEBUG) { "requestCancelOrder" }
        onOrderCancel(order, !isDropInCancelledByUser)
    }
}
