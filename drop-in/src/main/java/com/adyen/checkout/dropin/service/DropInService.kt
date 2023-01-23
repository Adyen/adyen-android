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

// TODO SESSIONS: check docs
/**
 * Base service to be extended by the merchant to provide the network calls that connect to the Adyen endpoints.
 * Calls should be made to your server, and from there to Adyen.
 *
 * The methods [makePaymentsCall] and [makeDetailsCall] are already run in the background and can return synchronously.
 * For async, you can override [onPaymentsCallRequested] and [onDetailsCallRequested] instead.
 * Check the documentation for more details.
 * The result [DropInServiceResult] is the result of the network call and can mean different things.
 * Check the subclasses of [DropInServiceResult] for more information.
 */
abstract class DropInService : BaseDropInService(), DropInServiceContract {

    override fun requestPaymentsCall(paymentComponentState: PaymentComponentState<*>) {
        Logger.d(TAG, "requestPaymentsCall")
        onSubmit(paymentComponentState)
    }

    override fun requestDetailsCall(actionComponentData: ActionComponentData) {
        Logger.d(TAG, "requestDetailsCall")
        onAdditionalDetails(actionComponentData)
    }

    override fun requestBalanceCall(paymentMethodData: PaymentMethodDetails) {
        Logger.d(TAG, "requestBalanceCall")
        onBalanceCheck(paymentMethodData)
    }

    override fun requestOrdersCall() {
        Logger.d(TAG, "requestOrdersCall")
        onOrderRequest()
    }

    override fun requestCancelOrder(order: OrderRequest, isDropInCancelledByUser: Boolean) {
        Logger.d(TAG, "requestCancelOrder")
        onOrderCancel(order, !isDropInCancelledByUser)
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
