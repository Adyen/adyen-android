/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/4/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.components.AdditionalDetailsResult
import com.adyen.checkout.core.components.SessionCheckoutCallbacks
import com.adyen.checkout.core.components.SubmitResult
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

    override suspend fun submit(data: PaymentComponentData<*>): SubmitResult {
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
                    response.action != null -> SubmitResult.Action(response.action)
                    else -> SubmitResult.Finished(response.resultCode.orEmpty())
                }
            },
            onFailure = { error ->
                // TODO - Add analytics
//                val event = GenericEvents.error(
//                    component = paymentMethodType,
//                    event = ErrorEvent.API_PAYMENTS,
//                )
//                analyticsManager.trackEvent(event)
                return SubmitResult.Error(
                    CheckoutError(
                        code = ErrorCode.HTTP,
                        message = error.message ?: "Failed to submit payment",
                        cause = error,
                    ),
                )
            },
        )
    }

    override suspend fun additionalDetails(data: ActionComponentData): AdditionalDetailsResult {
        sessionRepository.submitDetails(
            sessionId = sessionId,
            sessionData = sessionData,
            actionComponentData = data,
        ).fold(
            onSuccess = { response ->
                sessionData = response.sessionData
                callbacks.onFinished()
                return AdditionalDetailsResult.Finished(response.resultCode.orEmpty())
            },
            onFailure = { error ->
                val checkoutError = CheckoutError(
                    code = ErrorCode.HTTP,
                    message = "Failed to submit details",
                    cause = error,
                )
                callbacks.onError(checkoutError)
                return AdditionalDetailsResult.Error(checkoutError)
            },
        )
    }

    override fun error(error: CheckoutError) {
        callbacks.onError(error)
    }
}
