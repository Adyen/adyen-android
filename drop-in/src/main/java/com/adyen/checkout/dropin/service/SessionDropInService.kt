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
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.components.model.payments.response.BalanceResult
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.sessions.repository.SessionRepository
import kotlinx.coroutines.launch
import org.json.JSONObject

internal class SessionDropInService : DropInService() {

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
                        val result = when {
                            response.action != null -> DropInServiceResult.Action(response.action!!)
                            // TODO: figure out what to do in this case
                            response.order != null -> TODO()
                            else -> DropInServiceResult.Finished(response.resultCode ?: "EMPTY")
                        }
                        sendResult(result)
                    },
                    onFailure = {
                        val result = DropInServiceResult.Error(
                            reason = it.message,
                            dismissDropIn = false,
                        )
                        sendResult(result)
                    }
                )
        }
    }

    override fun onDetailsCallRequested(actionComponentData: ActionComponentData, actionComponentJson: JSONObject) {
        launch {
            sessionRepository.submitDetails(actionComponentData)
                .fold(
                    onSuccess = { response ->
                        val result = when {
                            response.action != null -> DropInServiceResult.Action(response.action!!)
                            else -> DropInServiceResult.Finished(response.resultCode ?: "EMPTY")
                        }
                        sendResult(result)
                    },
                    onFailure = {
                        val result = DropInServiceResult.Error(
                            reason = it.message,
                            dismissDropIn = false,
                        )
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
                        // TODO: Check how not enough balance is handled
                        val balanceResult = BalanceResult(response.balance, response.transactionLimit)
                        sendBalanceResult(BalanceDropInServiceResult.Balance(balanceResult))
                    },
                    onFailure = {
                        val result = DropInServiceResult.Error(
                            reason = it.message,
                            dismissDropIn = false,
                        )
                        sendResult(result)
                    }
                )
        }
    }

    override fun createOrder() {
        launch {
            sessionRepository.createOrder()
                .fold(
                    onSuccess = { response ->
                        // TODO: Check difference between SessionOrderResponse and OrderResponse
                    },
                    onFailure = {
                        val result = DropInServiceResult.Error(
                            reason = it.message,
                            dismissDropIn = false,
                        )
                        sendResult(result)
                    }
                )
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()

        internal const val INTENT_EXTRA_CONFIGURATION = "INTENT_EXTRA_CONFIGURATION"
        internal const val INTENT_EXTRA_SESSION = "INTENT_EXTRA_SESSION"
    }
}
