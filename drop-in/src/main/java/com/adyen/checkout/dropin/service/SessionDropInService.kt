/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/4/2022.
 */

package com.adyen.checkout.dropin.service

import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.components.model.payments.response.OrderResponse
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.sessions.api.SessionService
import com.adyen.checkout.sessions.interactor.SessionCallResult
import com.adyen.checkout.sessions.interactor.SessionInteractor
import com.adyen.checkout.sessions.model.SessionModel
import com.adyen.checkout.sessions.repository.SessionRepository
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import org.json.JSONObject

@Suppress("TooManyFunctions")
open class SessionDropInService : DropInService(), SessionDropInServiceInterface {

    private lateinit var sessionInteractor: SessionInteractor

    var isFlowTakenOver: Boolean = false
        private set

    final override fun initialize(
        sessionModel: SessionModel,
        clientKey: String,
        environment: Environment,
        isFlowTakenOver: Boolean
    ) {
        val httpClient = HttpClientFactory.getHttpClient(environment)
        val sessionService = SessionService(httpClient)
        sessionInteractor = SessionInteractor(
            sessionRepository = SessionRepository(
                sessionService = sessionService,
                clientKey = clientKey,
            ),
            sessionModel = sessionModel,
            isFlowTakenOver = isFlowTakenOver,
        )
        this.isFlowTakenOver = isFlowTakenOver

        launch {
            sessionInteractor.sessionFlow
                .mapNotNull { it.sessionData }
                .collect { sendSessionDataChangedResult(it) }
        }
    }

    private fun sendSessionDataChangedResult(sessionData: String) {
        Logger.d(TAG, "Sending session data changed result - $sessionData")
        val result = SessionDropInServiceResult.SessionDataChanged(sessionData)
        emitResult(result)
    }

    final override fun onPaymentsCallRequested(
        paymentComponentState: PaymentComponentState<*>,
        paymentComponentJson: JSONObject
    ) {
        launch {
            val result = sessionInteractor.onPaymentsCallRequested(
                paymentComponentState,
                { makePaymentsCallMerchant(it, paymentComponentJson) },
                ::makePaymentsCallMerchant.name,
            )

            val dropInServiceResult = when (result) {
                is SessionCallResult.Payments.Action -> DropInServiceResult.Action(result.action)
                is SessionCallResult.Payments.Error -> DropInServiceResult.Error(reason = result.throwable.message)
                is SessionCallResult.Payments.Finished -> DropInServiceResult.Finished(result.resultCode)
                is SessionCallResult.Payments.NotFullyPaidOrder -> updatePaymentMethods(result.order)
                is SessionCallResult.Payments.TakenOver -> {
                    sendFlowTakenOverUpdatedResult()
                    return@launch
                }
            }

            sendResult(dropInServiceResult)
        }
    }

    final override fun onDetailsCallRequested(
        actionComponentData: ActionComponentData,
        actionComponentJson: JSONObject
    ) {
        launch {
            val result = sessionInteractor.onDetailsCallRequested(
                actionComponentData,
                { makeDetailsCallMerchant(it, actionComponentJson) },
                ::makeDetailsCallMerchant.name
            )

            val dropInServiceResult = when (result) {
                is SessionCallResult.Details.Action -> DropInServiceResult.Action(result.action)
                is SessionCallResult.Details.Error -> DropInServiceResult.Error(reason = result.throwable.message)
                is SessionCallResult.Details.Finished -> DropInServiceResult.Finished(result.resultCode)
                SessionCallResult.Details.TakenOver -> {
                    sendFlowTakenOverUpdatedResult()
                    return@launch
                }
            }

            sendResult(dropInServiceResult)
        }
    }

    final override fun checkBalance(paymentMethodData: PaymentMethodDetails) {
        launch {
            val result = sessionInteractor.checkBalance(
                paymentMethodData,
                ::makeCheckBalanceCallMerchant,
                ::makeCheckBalanceCallMerchant.name,
            )

            val dropInServiceResult = when (result) {
                is SessionCallResult.Balance.Error ->
                    BalanceDropInServiceResult.Error(reason = result.throwable.message)
                is SessionCallResult.Balance.Successful -> BalanceDropInServiceResult.Balance(result.balanceResult)
                SessionCallResult.Balance.TakenOver -> {
                    sendFlowTakenOverUpdatedResult()
                    return@launch
                }
            }

            sendBalanceResult(dropInServiceResult)
        }
    }

    final override fun createOrder() {
        launch {
            val result = sessionInteractor.createOrder(
                ::makeCreateOrderMerchant,
                ::makeCreateOrderMerchant.name
            )

            val dropInServiceResult = when (result) {
                is SessionCallResult.CreateOrder.Error ->
                    OrderDropInServiceResult.Error(reason = result.throwable.message)
                is SessionCallResult.CreateOrder.Successful -> OrderDropInServiceResult.OrderCreated(result.order)
                SessionCallResult.CreateOrder.TakenOver -> {
                    sendFlowTakenOverUpdatedResult()
                    return@launch
                }
            }

            sendOrderResult(dropInServiceResult)
        }
    }

    final override fun cancelOrder(order: OrderRequest, shouldUpdatePaymentMethods: Boolean) {
        launch {
            val result = sessionInteractor.cancelOrder(
                order,
                { makeCancelOrderCallMerchant(it, shouldUpdatePaymentMethods) },
                ::makeCancelOrderCallMerchant.name
            )

            val dropInServiceResult = when (result) {
                is SessionCallResult.CancelOrder.Error -> DropInServiceResult.Error(reason = result.throwable.message)
                SessionCallResult.CancelOrder.Successful -> {
                    if (!shouldUpdatePaymentMethods) return@launch
                    updatePaymentMethods()
                }
                SessionCallResult.CancelOrder.TakenOver -> {
                    sendFlowTakenOverUpdatedResult()
                    return@launch
                }
            }

            sendResult(dropInServiceResult)
        }
    }

    private suspend fun updatePaymentMethods(order: OrderResponse? = null): DropInServiceResult {
        return when (val result = sessionInteractor.updatePaymentMethods(order)) {
            is SessionCallResult.UpdatePaymentMethods.Successful -> DropInServiceResult.Update(
                result.paymentMethods,
                result.order
            )
            is SessionCallResult.UpdatePaymentMethods.Error ->
                DropInServiceResult.Error(reason = result.throwable.message)
        }
    }

    private fun sendFlowTakenOverUpdatedResult() {
        if (isFlowTakenOver) return
        isFlowTakenOver = true
        Logger.i(TAG, "Flow was taken over, sending update to drop-in")
        val result = SessionDropInServiceResult.SessionTakenOverUpdated(true)
        emitResult(result)
    }

    protected open fun makePaymentsCallMerchant(
        paymentComponentState: PaymentComponentState<*>,
        paymentComponentJson: JSONObject
    ): Boolean = false

    protected open fun makeDetailsCallMerchant(
        actionComponentData: ActionComponentData,
        actionComponentJson: JSONObject
    ): Boolean = false

    protected open fun makeCheckBalanceCallMerchant(paymentMethodData: PaymentMethodDetails): Boolean = false

    protected open fun makeCreateOrderMerchant(): Boolean = false

    protected open fun makeCancelOrderCallMerchant(
        order: OrderRequest,
        shouldUpdatePaymentMethods: Boolean
    ): Boolean = false

    companion object {
        private val TAG = LogUtil.getTag()
    }
}

internal interface SessionDropInServiceInterface : DropInServiceInterface {
    fun initialize(
        sessionModel: SessionModel,
        clientKey: String,
        environment: Environment,
        isFlowTakenOver: Boolean
    )
}
