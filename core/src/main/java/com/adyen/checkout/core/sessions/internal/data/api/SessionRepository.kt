/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/5/2025.
 */

package com.adyen.checkout.core.sessions.internal.data.api

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.common.internal.helper.runSuspendCatching
import com.adyen.checkout.core.components.data.OrderRequest
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.sessions.internal.data.model.SessionDetailsRequest
import com.adyen.checkout.core.sessions.internal.data.model.SessionDetailsResponse
import com.adyen.checkout.core.sessions.internal.data.model.SessionPaymentsRequest
import com.adyen.checkout.core.sessions.internal.data.model.SessionPaymentsResponse
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupRequest
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupResponse

internal class SessionRepository(
    private val sessionService: SessionService,
    private val clientKey: String,
) {

    @Suppress("CommentWrapping")
    suspend fun setupSession(
        sessionId: String,
        sessionData: String?,
        order: OrderRequest?,
    ): Result<SessionSetupResponse> = runSuspendCatching {
        val request = SessionSetupRequest(sessionData = sessionData.orEmpty(), order = order)
        sessionService.setupSession(
            request = request,
            sessionId = sessionId,
            clientKey = clientKey,
        )
    }

    suspend fun submitPayment(
        sessionId: String,
        sessionData: String?,
        paymentComponentData: PaymentComponentData<out PaymentMethodDetails>
    ): Result<SessionPaymentsResponse> = runSuspendCatching {
        val request = SessionPaymentsRequest(sessionData.orEmpty(), paymentComponentData)
        sessionService.submitPayment(
            request = request,
            sessionId = sessionId,
            clientKey = clientKey,
        )
    }

    suspend fun submitDetails(
        sessionId: String,
        sessionData: String?,
        actionComponentData: ActionComponentData
    ): Result<SessionDetailsResponse> = runSuspendCatching {
        val request = SessionDetailsRequest(
            sessionData = sessionData.orEmpty(),
            paymentData = actionComponentData.paymentData,
            details = actionComponentData.details,
        )
        sessionService.submitDetails(
            request = request,
            sessionId = sessionId,
            clientKey = clientKey,
        )
    }
}
