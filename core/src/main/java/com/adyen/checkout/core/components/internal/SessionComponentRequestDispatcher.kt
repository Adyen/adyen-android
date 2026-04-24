/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/4/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.SessionCheckoutCallbacks
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.error.CheckoutError.ErrorCode
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository

internal class SessionComponentRequestDispatcher(
    initialSessionData: String?,
    private val sessionId: String,
    private val callbacks: SessionCheckoutCallbacks,
    private val sessionRepository: SessionRepository,
) : ComponentRequestDispatcher {

    private var sessionData: String? = initialSessionData

    override suspend fun submit(data: PaymentComponentData<*>): CheckoutResult {
        // TODO - Session patching
        callbacks.beforeSubmit?.invoke(data)
        sessionRepository.submitPayment(
            sessionId = sessionId,
            sessionData = sessionData,
            paymentComponentData = data,
        ).fold(
            onSuccess = { response ->
                sessionData = response.sessionData
                // TODO - Check if we need to support partial payment flow
                return when {
                    response.action != null -> CheckoutResult.Action(response.action)
                    else -> CheckoutResult.Finished(response.resultCode)
                }
            },
            onFailure = { error ->
                // TODO - Add analytics
//                val event = GenericEvents.error(
//                    component = paymentMethodType,
//                    event = ErrorEvent.API_PAYMENTS,
//                )
//                analyticsManager.trackEvent(event)
                return CheckoutResult.Error(error.message ?: "Failed to submit payment")
            },
        )
    }

    override suspend fun additionalDetails(data: ActionComponentData) {
        sessionRepository.submitDetails(
            sessionId = sessionId,
            sessionData = sessionData,
            actionComponentData = data,
        ).fold(
            onSuccess = { response ->
                sessionData = response.sessionData
                callbacks.onFinished()
            },
            onFailure = { error ->
                callbacks.onError(
                    CheckoutError(
                        code = ErrorCode.HTTP,
                        message = "Failed to submit details",
                        cause = error,
                    ),
                )
            },
        )
    }

    override fun error(error: CheckoutError) {
        callbacks.onError(error)
    }
}
