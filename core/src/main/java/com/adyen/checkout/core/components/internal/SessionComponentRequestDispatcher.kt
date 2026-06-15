/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/4/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.common.CheckoutResultCode
import com.adyen.checkout.core.components.AdditionalDetailsResult
import com.adyen.checkout.core.components.SessionCheckoutCallbacks
import com.adyen.checkout.core.components.SessionCheckoutResult
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.error.toCheckoutError
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository

internal class SessionComponentRequestDispatcher(
    initialSessionData: String,
    private val sessionId: String,
    private val callbacks: SessionCheckoutCallbacks,
    private val sessionRepository: SessionRepository,
) : SubmittableComponentRequestDispatcher {

    private var sessionData: String = initialSessionData

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
                    else -> {
                        SubmitResult.Completion(response.resultCode ?: RESULT_CODE_MISSING)
                    }
                }
            },
            onFailure = { error ->
                // TODO - Add analytics
//                val event = GenericEvents.error(
//                    component = paymentMethodType,
//                    event = ErrorEvent.API_PAYMENTS,
//                )
//                analyticsManager.trackEvent(event)
                callbacks.onFailure(error.toCheckoutError())
                return SubmitResult.Retry(error.message)
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
                return AdditionalDetailsResult.Completion(response.resultCode ?: RESULT_CODE_MISSING)
            },
            onFailure = { error ->
                callbacks.onFailure(error.toCheckoutError())
                return AdditionalDetailsResult.Completion(CheckoutResultCode.ERROR.value)
            },
        )
    }

    override fun complete(resultCode: CheckoutResultCode) {
        val result = SessionCheckoutResult(
            resultCode = resultCode,
            sessionId = sessionId,
            sessionData = sessionData,
        )
        callbacks.onComplete(result)
    }

    override fun failure(error: CheckoutError) {
        callbacks.onFailure(error)
    }

    companion object {
        private const val RESULT_CODE_MISSING = "Unknown"
    }
}
