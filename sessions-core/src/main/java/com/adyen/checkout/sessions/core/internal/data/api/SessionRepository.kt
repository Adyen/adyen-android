/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2022.
 */

package com.adyen.checkout.sessions.core.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.old.internal.util.runSuspendCatching
import com.adyen.checkout.sessions.core.SessionModel
import com.adyen.checkout.sessions.core.SessionSetupResponse
import com.adyen.checkout.sessions.core.internal.data.model.SessionBalanceRequest
import com.adyen.checkout.sessions.core.internal.data.model.SessionBalanceResponse
import com.adyen.checkout.sessions.core.internal.data.model.SessionCancelOrderRequest
import com.adyen.checkout.sessions.core.internal.data.model.SessionCancelOrderResponse
import com.adyen.checkout.sessions.core.internal.data.model.SessionDetailsRequest
import com.adyen.checkout.sessions.core.internal.data.model.SessionDetailsResponse
import com.adyen.checkout.sessions.core.internal.data.model.SessionDisableTokenRequest
import com.adyen.checkout.sessions.core.internal.data.model.SessionDisableTokenResponse
import com.adyen.checkout.sessions.core.internal.data.model.SessionOrderRequest
import com.adyen.checkout.sessions.core.internal.data.model.SessionOrderResponse
import com.adyen.checkout.sessions.core.internal.data.model.SessionPaymentsRequest
import com.adyen.checkout.sessions.core.internal.data.model.SessionPaymentsResponse
import com.adyen.checkout.sessions.core.internal.data.model.SessionSetupRequest

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SessionRepository(
    private val sessionService: SessionService,
    private val clientKey: String,
) {

    suspend fun setupSession(
        sessionModel: SessionModel,
        order: OrderRequest?
    ): Result<SessionSetupResponse> = runSuspendCatching {
        val request = SessionSetupRequest(sessionModel.sessionData.orEmpty(), order)
        sessionService.setupSession(
            request = request,
            sessionId = sessionModel.id,
            clientKey = clientKey,
        )
    }

    suspend fun submitPayment(
        sessionModel: SessionModel,
        paymentComponentData: PaymentComponentData<out PaymentMethodDetails>
    ): Result<SessionPaymentsResponse> = runSuspendCatching {
        val request = SessionPaymentsRequest(sessionModel.sessionData.orEmpty(), paymentComponentData)
        sessionService.submitPayment(
            request = request,
            sessionId = sessionModel.id,
            clientKey = clientKey,
        )
    }

    suspend fun submitDetails(
        sessionModel: SessionModel,
        actionComponentData: ActionComponentData
    ): Result<SessionDetailsResponse> = runSuspendCatching {
        val request = SessionDetailsRequest(
            sessionData = sessionModel.sessionData.orEmpty(),
            paymentData = actionComponentData.paymentData,
            details = actionComponentData.details,
        )
        sessionService.submitDetails(
            request = request,
            sessionId = sessionModel.id,
            clientKey = clientKey,
        )
    }

    suspend fun checkBalance(
        sessionModel: SessionModel,
        paymentComponentState: PaymentComponentState<*>
    ): Result<SessionBalanceResponse> = runSuspendCatching {
        val request = SessionBalanceRequest(
            sessionModel.sessionData.orEmpty(),
            paymentComponentState.data.paymentMethod,
            paymentComponentState.data.amount,
        )
        sessionService.checkBalance(
            request = request,
            sessionId = sessionModel.id,
            clientKey = clientKey,
        )
    }

    suspend fun createOrder(sessionModel: SessionModel): Result<SessionOrderResponse> = runSuspendCatching {
        val request = SessionOrderRequest(sessionModel.sessionData.orEmpty())
        sessionService.createOrder(
            request = request,
            sessionId = sessionModel.id,
            clientKey = clientKey,
        )
    }

    suspend fun cancelOrder(
        sessionModel: SessionModel,
        order: OrderRequest
    ): Result<SessionCancelOrderResponse> = runSuspendCatching {
        val request = SessionCancelOrderRequest(sessionModel.sessionData.orEmpty(), order)
        sessionService.cancelOrder(
            request = request,
            sessionId = sessionModel.id,
            clientKey = clientKey,
        )
    }

    suspend fun disableToken(
        sessionModel: SessionModel,
        storedPaymentMethodId: String,
    ): Result<SessionDisableTokenResponse> = runSuspendCatching {
        val request = SessionDisableTokenRequest(sessionModel.sessionData.orEmpty(), storedPaymentMethodId)
        sessionService.disableToken(
            request = request,
            sessionId = sessionModel.id,
            clientKey = clientKey,
        )
    }
}
