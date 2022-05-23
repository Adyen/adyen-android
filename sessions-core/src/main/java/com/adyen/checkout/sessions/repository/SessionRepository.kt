/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2022.
 */

package com.adyen.checkout.sessions.repository

import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.util.runSuspendCatching
import com.adyen.checkout.sessions.api.SessionService
import com.adyen.checkout.sessions.model.Session
import com.adyen.checkout.sessions.model.orders.SessionBalanceRequest
import com.adyen.checkout.sessions.model.orders.SessionBalanceResponse
import com.adyen.checkout.sessions.model.orders.SessionCancelOrderRequest
import com.adyen.checkout.sessions.model.orders.SessionCancelOrderResponse
import com.adyen.checkout.sessions.model.orders.SessionOrderRequest
import com.adyen.checkout.sessions.model.orders.SessionOrderResponse
import com.adyen.checkout.sessions.model.payments.SessionDetailsRequest
import com.adyen.checkout.sessions.model.payments.SessionDetailsResponse
import com.adyen.checkout.sessions.model.payments.SessionPaymentsRequest
import com.adyen.checkout.sessions.model.payments.SessionPaymentsResponse
import com.adyen.checkout.sessions.model.setup.SessionSetupRequest
import com.adyen.checkout.sessions.model.setup.SessionSetupResponse

class SessionRepository(
    baseUrl: String,
    private val clientKey: String,
    private var session: Session,
) {

    private val sessionService = SessionService(baseUrl)

    suspend fun setupSession(
        order: OrderRequest?
    ): Result<SessionSetupResponse> = runSuspendCatching {
        Logger.d(TAG, "Setting up session")

        val request = SessionSetupRequest(session.sessionData.orEmpty(), order)
        sessionService.setupSession(
            request = request,
            sessionId = session.id,
            clientKey = clientKey
        ).also {
            session = session.copy(sessionData = it.sessionData)
        }
    }

    suspend fun submitPayment(
        paymentComponentData: PaymentComponentData<out PaymentMethodDetails>
    ): Result<SessionPaymentsResponse> = runSuspendCatching {
        Logger.d(TAG, "Submitting payment")

        val request = SessionPaymentsRequest(session.sessionData.orEmpty(), paymentComponentData)
        sessionService.submitPayment(
            request = request,
            sessionId = session.id,
            clientKey = clientKey
        ).also {
            session = session.copy(sessionData = it.sessionData)
        }
    }

    suspend fun submitDetails(
        actionComponentData: ActionComponentData
    ): Result<SessionDetailsResponse> = runSuspendCatching {
        Logger.d(TAG, "Submitting details")

        val request = SessionDetailsRequest(
            sessionData = session.sessionData.orEmpty(),
            paymentData = actionComponentData.paymentData,
            details = actionComponentData.details
        )
        sessionService.submitDetails(
            request = request,
            sessionId = session.id,
            clientKey = clientKey
        ).also {
            session = session.copy(sessionData = it.sessionData)
        }
    }

    suspend fun checkBalance(
        paymentMethodDetails: PaymentMethodDetails
    ): Result<SessionBalanceResponse> = runSuspendCatching {
        Logger.d(TAG, "Checking payment method balance")

        val request = SessionBalanceRequest(session.sessionData.orEmpty(), paymentMethodDetails)
        sessionService.checkBalance(
            request = request,
            sessionId = session.id,
            clientKey = clientKey
        ).also {
            session = session.copy(sessionData = it.sessionData)
        }
    }

    suspend fun createOrder(): Result<SessionOrderResponse> = runSuspendCatching {
        Logger.d(TAG, "Creating order")

        val request = SessionOrderRequest(session.sessionData.orEmpty())
        sessionService.createOrder(
            request = request,
            sessionId = session.id,
            clientKey = clientKey
        ).also {
            session = session.copy(sessionData = it.sessionData)
        }
    }

    suspend fun cancelOrder(
        order: OrderRequest
    ): Result<SessionCancelOrderResponse> = runSuspendCatching {
        Logger.d(TAG, "Cancelling order")

        val request = SessionCancelOrderRequest(session.sessionData.orEmpty(), order)
        sessionService.cancelOrder(
            request = request,
            sessionId = session.id,
            clientKey = clientKey
        ).also {
            session = session.copy(sessionData = it.sessionData)
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
