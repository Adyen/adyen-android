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

@Suppress("TooManyFunctions")
open class SessionDropInService : BaseDropInService(), SessionDropInServiceInterface, SessionDropInServiceContract {

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

    override fun requestPaymentsCall(paymentComponentState: PaymentComponentState<*>) {
        launch {
            val result = sessionInteractor.onPaymentsCallRequested(
                paymentComponentState,
                ::onSubmit,
                ::onSubmit.name,
            )

            val dropInServiceResult = when (result) {
                is SessionCallResult.Payments.Action -> DropInServiceResult.Action(result.action)
                is SessionCallResult.Payments.Error ->
                    DropInServiceResult.Error(reason = result.throwable.message, dismissDropIn = true)
                is SessionCallResult.Payments.Finished -> DropInServiceResult.Finished(result.result.resultCode ?: "")
                is SessionCallResult.Payments.NotFullyPaidOrder -> updatePaymentMethods(result.result.order)
                is SessionCallResult.Payments.RefusedPartialPayment ->
                    DropInServiceResult.Error(reason = "Payment is refused while making a partial payment.")
                is SessionCallResult.Payments.TakenOver -> {
                    sendFlowTakenOverUpdatedResult()
                    return@launch
                }
            }

            sendResult(dropInServiceResult)
        }
    }

    override fun requestDetailsCall(actionComponentData: ActionComponentData) {
        launch {
            val result = sessionInteractor.onDetailsCallRequested(
                actionComponentData,
                ::onAdditionalDetails,
                ::onAdditionalDetails.name
            )

            val dropInServiceResult = when (result) {
                is SessionCallResult.Details.Action -> DropInServiceResult.Action(result.action)
                is SessionCallResult.Details.Error ->
                    DropInServiceResult.Error(reason = result.throwable.message, dismissDropIn = true)
                is SessionCallResult.Details.Finished -> DropInServiceResult.Finished(result.result.resultCode ?: "")
                SessionCallResult.Details.TakenOver -> {
                    sendFlowTakenOverUpdatedResult()
                    return@launch
                }
            }

            sendResult(dropInServiceResult)
        }
    }

    override fun requestBalanceCall(paymentMethodData: PaymentMethodDetails) {
        launch {
            val result = sessionInteractor.checkBalance(
                paymentMethodData,
                ::onBalanceCheck,
                ::onBalanceCheck.name,
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

    override fun requestOrdersCall() {
        launch {
            val result = sessionInteractor.createOrder(
                ::onOrderRequest,
                ::onOrderRequest.name
            )

            val dropInServiceResult = when (result) {
                is SessionCallResult.CreateOrder.Error ->
                    OrderDropInServiceResult.Error(reason = result.throwable.message, dismissDropIn = true)
                is SessionCallResult.CreateOrder.Successful -> OrderDropInServiceResult.OrderCreated(result.order)
                SessionCallResult.CreateOrder.TakenOver -> {
                    sendFlowTakenOverUpdatedResult()
                    return@launch
                }
            }

            sendOrderResult(dropInServiceResult)
        }
    }

    override fun requestCancelOrder(order: OrderRequest, isDropInCancelledByUser: Boolean) {
        val shouldUpdatePaymentMethods = !isDropInCancelledByUser
        launch {
            val result = sessionInteractor.cancelOrder(
                order,
                { onOrderCancel(it, shouldUpdatePaymentMethods) },
                ::onOrderCancel.name
            )

            val dropInServiceResult = when (result) {
                is SessionCallResult.CancelOrder.Error ->
                    DropInServiceResult.Error(reason = result.throwable.message)
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
                DropInServiceResult.Error(reason = result.throwable.message, dismissDropIn = true)
        }
    }

    private fun sendFlowTakenOverUpdatedResult() {
        if (isFlowTakenOver) return
        isFlowTakenOver = true
        Logger.i(TAG, "Flow was taken over, sending update to drop-in")
        val result = SessionDropInServiceResult.SessionTakenOverUpdated(true)
        emitResult(result)
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
