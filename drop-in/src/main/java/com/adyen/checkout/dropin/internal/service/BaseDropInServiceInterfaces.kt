/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/1/2023.
 */

package com.adyen.checkout.dropin.internal.service

import com.adyen.checkout.card.BinLookupData
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.dropin.BaseDropInServiceResult
import com.adyen.checkout.sessions.core.SessionModel

@Suppress("TooManyFunctions")
internal interface BaseDropInServiceInterface {
    suspend fun observeResult(callback: (BaseDropInServiceResult) -> Unit)
    fun requestPaymentsCall(paymentComponentState: PaymentComponentState<*>)
    fun requestDetailsCall(actionComponentData: ActionComponentData)
    fun requestBalanceCall(paymentComponentState: PaymentComponentState<*>)
    fun requestOrdersCall()
    fun requestCancelOrder(order: OrderRequest, isDropInCancelledByUser: Boolean)
    fun requestRemoveStoredPaymentMethod(storedPaymentMethod: StoredPaymentMethod)
    fun onRedirectCalled()
    fun onBinValueCalled(binValue: String)
    fun onBinLookupCalled(data: List<BinLookupData>)
    fun onAddressLookupQueryChangedCalled(query: String)
    fun onAddressLookupCompletionCalled(lookupAddress: LookupAddress): Boolean
}

internal interface SessionDropInServiceInterface : BaseDropInServiceInterface {
    fun initialize(
        sessionModel: SessionModel,
        clientKey: String,
        environment: Environment,
        isFlowTakenOver: Boolean,
        analyticsManager: AnalyticsManager,
    )
}
