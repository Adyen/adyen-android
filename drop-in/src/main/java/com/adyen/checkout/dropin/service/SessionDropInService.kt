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
import com.adyen.checkout.components.model.payments.response.BalanceResult
import com.adyen.checkout.components.model.payments.response.OrderResponse
import com.adyen.checkout.components.status.api.StatusResponseUtils.RESULT_REFUSED
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.sessions.api.SessionService
import com.adyen.checkout.sessions.model.Session
import com.adyen.checkout.sessions.model.payments.SessionPaymentsResponse
import com.adyen.checkout.sessions.repository.SessionRepository
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import org.json.JSONObject

@Suppress("TooManyFunctions")
open class SessionDropInService : DropInService(), SessionDropInServiceInterface {

    private lateinit var sessionRepository: SessionRepository

    override fun initialize(session: Session, clientKey: String, baseUrl: String, shouldFetchPaymentMethods: Boolean) {
        val sessionService = SessionService(baseUrl)
        sessionRepository = SessionRepository(sessionService = sessionService, clientKey = clientKey, session = session)

        launch {
            sessionRepository.sessionFlow
                .mapNotNull { it.sessionData }
                .collect { sendSessionDataChangedResult(it) }
        }

        if (shouldFetchPaymentMethods) {
            setupSession()
        }
    }

    private fun sendSessionDataChangedResult(sessionData: String) {
        Logger.d(TAG, "Sending session data changed result - $sessionData")
        val result = SessionDropInServiceResult.SessionDataChanged(sessionData)
        emitResult(result)
    }

    private fun setupSession() {
        launch {
            sessionRepository.setupSession(null)
                .fold(
                    onSuccess = {
                        sendSessionSetupResult(SessionDropInServiceResult.SetupDone(it.paymentMethods))
                    },
                    onFailure = {
                        val result = SessionDropInServiceResult.Error(reason = it.message, dismissDropIn = true)
                        sendSessionSetupResult(result)
                    }
                )
        }
    }

    private fun sendSessionSetupResult(sessionDropInServiceResult: SessionDropInServiceResult) {
        Logger.d(TAG, "Sending session setup result")
        emitResult(sessionDropInServiceResult)
    }

    override fun onPaymentsCallRequested(
        paymentComponentState: PaymentComponentState<*>,
        paymentComponentJson: JSONObject
    ) {
        launch {
            sessionRepository.submitPayment(paymentComponentState.data)
                .fold(
                    onSuccess = { response ->
                        val action = response.action
                        val result = when {
                            response.isRefused() -> DropInServiceResult.Error(reason = response.resultCode)
                            action != null -> DropInServiceResult.Action(action)
                            response.order.isNonFullyPaid() -> {
                                updatePaymentMethods(response.order)
                                null
                            }
                            else -> DropInServiceResult.Finished(response.resultCode ?: "EMPTY")
                        }
                        result?.let { sendResult(result) }
                    },
                    onFailure = {
                        val result = DropInServiceResult.Error(reason = it.message)
                        sendResult(result)
                    }
                )
        }
    }

    private fun SessionPaymentsResponse.isRefused() = resultCode.equals(other = RESULT_REFUSED, ignoreCase = true)

    private fun OrderResponse?.isNonFullyPaid() = (this?.remainingAmount?.value ?: 0) > 0

    override fun onDetailsCallRequested(actionComponentData: ActionComponentData, actionComponentJson: JSONObject) {
        launch {
            sessionRepository.submitDetails(actionComponentData)
                .fold(
                    onSuccess = { response ->
                        val result = when (val action = response.action) {
                            null -> DropInServiceResult.Finished(response.resultCode ?: "EMPTY")
                            else -> DropInServiceResult.Action(action)
                        }
                        sendResult(result)
                    },
                    onFailure = {
                        val result = DropInServiceResult.Error(reason = it.message)
                        sendResult(result)
                    }
                )
        }
    }

    override fun checkBalance(paymentMethodData: PaymentMethodDetails) {
        launch {
            sessionRepository.checkBalance(paymentMethodData)
                .fold(
                    onSuccess = { response ->
                        val result = if (response.balance.value <= 0) {
                            BalanceDropInServiceResult.Error(reason = "Not enough balance")
                        } else {
                            val balanceResult = BalanceResult(response.balance, response.transactionLimit)
                            BalanceDropInServiceResult.Balance(balanceResult)
                        }
                        sendBalanceResult(result)
                    },
                    onFailure = {
                        val result = BalanceDropInServiceResult.Error(reason = it.message)
                        sendBalanceResult(result)
                    }
                )
        }
    }

    override fun createOrder() {
        launch {
            sessionRepository.createOrder()
                .fold(
                    onSuccess = { response ->
                        val order = OrderResponse(
                            pspReference = response.pspReference,
                            orderData = response.orderData,
                            reference = null,
                            amount = null,
                            remainingAmount = null,
                            expiresAt = null,
                        )
                        sendOrderResult(OrderDropInServiceResult.OrderCreated(order))
                    },
                    onFailure = {
                        val result = OrderDropInServiceResult.Error(reason = it.message)
                        sendOrderResult(result)
                    }
                )
        }
    }

    override fun cancelOrder(order: OrderRequest, shouldUpdatePaymentMethods: Boolean) {
        launch {
            sessionRepository.cancelOrder(order)
                .fold(
                    onSuccess = {
                        if (shouldUpdatePaymentMethods) {
                            updatePaymentMethods()
                        }
                    },
                    onFailure = {
                        val result = SessionDropInServiceResult.Error(reason = it.message)
                        sendSessionSetupResult(result)
                    }
                )
        }
    }

    private fun updatePaymentMethods(order: OrderResponse? = null) {
        launch {
            val orderRequest = order?.let {
                OrderRequest(
                    pspReference = order.pspReference,
                    orderData = order.orderData
                )
            }

            sessionRepository.setupSession(orderRequest)
                .fold(
                    onSuccess = { response ->
                        val paymentMethods = response.paymentMethods
                        val result = if (paymentMethods != null) DropInServiceResult.Update(paymentMethods, order)
                        else DropInServiceResult.Error(reason = "Payment methods should not be null")
                        sendResult(result)
                    },
                    onFailure = {
                        val result = DropInServiceResult.Error(reason = it.message)
                        sendResult(result)
                    }
                )
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}

internal interface SessionDropInServiceInterface : DropInServiceInterface {
    fun initialize(session: Session, clientKey: String, baseUrl: String, shouldFetchPaymentMethods: Boolean)
}
