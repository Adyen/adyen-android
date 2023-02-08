/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.service

import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

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
        Logger.d(TAG, "requestPaymentsCall")
        onSubmit(paymentComponentState)
    }

    final override fun requestDetailsCall(actionComponentData: ActionComponentData) {
        Logger.d(TAG, "requestDetailsCall")
        onAdditionalDetails(actionComponentData)
    }

    final override fun requestBalanceCall(paymentMethodData: PaymentMethodDetails) {
        Logger.d(TAG, "requestBalanceCall")
        onBalanceCheck(paymentMethodData)
    }

    final override fun requestOrdersCall() {
        Logger.d(TAG, "requestOrdersCall")
        onOrderRequest()
    }

    final override fun requestCancelOrder(order: OrderRequest, isDropInCancelledByUser: Boolean) {
        Logger.d(TAG, "requestCancelOrder")
        onOrderCancel(order, !isDropInCancelledByUser)
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
