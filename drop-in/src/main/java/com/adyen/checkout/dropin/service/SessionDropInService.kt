/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/4/2022.
 */

package com.adyen.checkout.dropin.service

import android.content.Intent
import android.os.IBinder
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.components.model.payments.response.BalanceResult
import com.adyen.checkout.components.model.payments.response.OrderResponse
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.sessions.repository.SessionRepository
import kotlinx.coroutines.launch
import org.json.JSONObject

open class SessionDropInService : DropInService() {

    private var isInitialized = false

    private lateinit var sessionRepository: SessionRepository

    override fun onBind(intent: Intent?): IBinder {
        val binder = super.onBind(intent)
        val additionalData = getAdditionalData()

        if (
            !isInitialized &&
            additionalData != null
        ) {
            val configuration = requireNotNull(additionalData.getParcelable<Configuration>(INTENT_EXTRA_CONFIGURATION))
            sessionRepository = SessionRepository(
                configuration = configuration,
                session = requireNotNull(additionalData.getParcelable(INTENT_EXTRA_SESSION)),
            )

            setupSession()

            isInitialized = true
        }

        return binder
    }

    private fun setupSession() {
        launch {
            sessionRepository.setupSession(null)
                .fold(
                    onSuccess = {
                        sendSessionSetupResult(SessionSetupDropInServiceResult.Success(it))
                    },
                    onFailure = {
                        val result = SessionSetupDropInServiceResult.Error(
                            reason = it.message,
                            dismissDropIn = false,
                        )
                        sendSessionSetupResult(result)
                    }
                )
        }
    }

    private fun sendSessionSetupResult(sessionSetupDropInServiceResult: SessionSetupDropInServiceResult) {
        Logger.d(TAG, "Sending session setup result")
        emitResult(sessionSetupDropInServiceResult)
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
                            action != null -> DropInServiceResult.Action(action)
                            response.order.isNonFullyPaid() -> {
                                updatePaymentMethods(response.order)
                                null
                            }
                            else -> DropInServiceResult.Finished(response.resultCode ?: "EMPTY")
                        }
                        result?.let { sendResult(result) }
                    },
                    onFailure = ::onFailure
                )
        }
    }

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
                    onFailure = ::onFailure
                )
        }
    }

    override fun checkBalance(paymentMethodData: PaymentMethodDetails) {
        launch {
            sessionRepository.checkBalance(paymentMethodData)
                .fold(
                    onSuccess = { response ->
                        val balanceResult = BalanceResult(response.balance, response.transactionLimit)
                        sendBalanceResult(BalanceDropInServiceResult.Balance(balanceResult))
                    },
                    onFailure = ::onFailure
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
                    onFailure = ::onFailure
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
                    onFailure = ::onFailure
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
                    onFailure = ::onFailure
                )
        }
    }

    private fun onFailure(throwable: Throwable) {
        val result = DropInServiceResult.Error(
            reason = throwable.message,
            dismissDropIn = false,
        )
        sendResult(result)
    }

    companion object {
        private val TAG = LogUtil.getTag()

        internal const val INTENT_EXTRA_CONFIGURATION = "INTENT_EXTRA_CONFIGURATION"
        internal const val INTENT_EXTRA_SESSION = "INTENT_EXTRA_SESSION"
    }
}
