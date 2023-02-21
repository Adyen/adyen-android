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
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.sessions.SessionModel

internal interface BaseDropInServiceInterface {
    suspend fun observeResult(callback: (BaseDropInServiceResult) -> Unit)
    fun requestPaymentsCall(paymentComponentState: PaymentComponentState<*>)
    fun requestDetailsCall(actionComponentData: ActionComponentData)
    fun requestBalanceCall(paymentMethodData: PaymentMethodDetails)
    fun requestOrdersCall()
    fun requestCancelOrder(order: OrderRequest, isDropInCancelledByUser: Boolean)
    fun requestRemoveStoredPaymentMethod(storedPaymentMethod: StoredPaymentMethod)
}

internal interface SessionDropInServiceInterface : BaseDropInServiceInterface {
    fun initialize(
        sessionModel: SessionModel,
        clientKey: String,
        environment: Environment,
        isFlowTakenOver: Boolean
    )
}
