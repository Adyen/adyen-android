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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class SessionRepository(
    private val sessionService: SessionService,
    private val clientKey: String,
    session: Session,
) {

    private val _sessionFlow = MutableStateFlow(session)
    val sessionFlow: Flow<Session> = _sessionFlow

    private val session: Session get() = _sessionFlow.value

    suspend fun setupSession(
        order: OrderRequest?
    ): Result<SessionSetupResponse> = runSuspendCatching {
        val request = SessionSetupRequest(session.sessionData.orEmpty(), order)
        sessionService.setupSession(
            request = request,
            sessionId = session.id,
            clientKey = clientKey
        ).also {
            updateSessionData(it.sessionData)
        }
    }

    suspend fun submitPayment(
        paymentComponentData: PaymentComponentData<out PaymentMethodDetails>
    ): Result<SessionPaymentsResponse> = runSuspendCatching {
        val request = SessionPaymentsRequest(session.sessionData.orEmpty(), paymentComponentData)
        sessionService.submitPayment(
            request = request,
            sessionId = session.id,
            clientKey = clientKey
        ).also {
            updateSessionData(it.sessionData)
        }
    }

    suspend fun submitDetails(
        actionComponentData: ActionComponentData
    ): Result<SessionDetailsResponse> = runSuspendCatching {
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
            updateSessionData(it.sessionData)
        }
    }

    suspend fun checkBalance(
        paymentMethodDetails: PaymentMethodDetails
    ): Result<SessionBalanceResponse> = runSuspendCatching {
        val request = SessionBalanceRequest(session.sessionData.orEmpty(), paymentMethodDetails)
        sessionService.checkBalance(
            request = request,
            sessionId = session.id,
            clientKey = clientKey
        ).also {
            updateSessionData(it.sessionData)
        }
    }

    suspend fun createOrder(): Result<SessionOrderResponse> = runSuspendCatching {
        val request = SessionOrderRequest(session.sessionData.orEmpty())
        sessionService.createOrder(
            request = request,
            sessionId = session.id,
            clientKey = clientKey
        ).also {
            updateSessionData(it.sessionData)
        }
    }

    suspend fun cancelOrder(
        order: OrderRequest
    ): Result<SessionCancelOrderResponse> = runSuspendCatching {
        val request = SessionCancelOrderRequest(session.sessionData.orEmpty(), order)
        sessionService.cancelOrder(
            request = request,
            sessionId = session.id,
            clientKey = clientKey
        ).also {
            updateSessionData(it.sessionData)
        }
    }

    private fun updateSessionData(sessionData: String) {
        _sessionFlow.update { it.copy(sessionData = sessionData) }
    }
}
