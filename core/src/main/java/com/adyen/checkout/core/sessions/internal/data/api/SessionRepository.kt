/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/5/2025.
 */

package com.adyen.checkout.core.sessions.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.internal.helper.runSuspendCatching
import com.adyen.checkout.core.components.data.OrderRequest
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.sessions.SessionModel
import com.adyen.checkout.core.sessions.internal.data.model.SessionPaymentsRequest
import com.adyen.checkout.core.sessions.internal.data.model.SessionPaymentsResponse
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupRequest
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupResponse

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SessionRepository(
    private val sessionService: SessionService,
    private val clientKey: String,
) {

    @Suppress("CommentWrapping")
    suspend fun setupSession(
        sessionModel: SessionModel,
        order: OrderRequest?,
    ): Result<SessionSetupResponse> = runSuspendCatching {
        val request = SessionSetupRequest(sessionData = sessionModel.sessionData.orEmpty(), order = order)
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
}
