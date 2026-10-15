/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/4/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.action.data.ActionData
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.CheckoutResultCode
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.AdditionalDetailsResult
import com.adyen.checkout.core.components.BeforeSubmitResult
import com.adyen.checkout.core.components.SessionCheckoutCallbacks
import com.adyen.checkout.core.components.SessionCheckoutResult
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.data.BeforeSubmitData
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.data.applyBeforeSubmitData
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.error.toCheckoutError
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.internal.SessionStateStore
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository

internal class SessionComponentRequestDispatcher(
    private val checkoutSession: CheckoutSession,
    private val callbacks: SessionCheckoutCallbacks,
    private val sessionRepository: SessionRepository,
    private val sessionStateStore: SessionStateStore,
) : SubmittableComponentRequestDispatcher {

    private val sessionId: String = checkoutSession.sessionSetupResponse.id

    private var sessionResult: String = ""

    override suspend fun submit(data: PaymentComponentData<*>): SubmitResult {
        val beforeSubmitResult = handleBeforeSubmit(data)

        return when (beforeSubmitResult) {
            is BeforeSubmitResult.Abort -> SubmitResult.Retry()
            is BeforeSubmitResult.Proceed -> {
                beforeSubmitResult.sessionData?.let { sessionStateStore.setSessionData(sessionId, it) }
                val finalData = data.applyBeforeSubmitData(beforeSubmitResult.data)
                sessionRepository.submitPayment(
                    sessionId = sessionId,
                    sessionData = sessionStateStore.getSessionData(checkoutSession),
                    paymentComponentData = finalData,
                ).fold(
                    onSuccess = { response ->
                        sessionStateStore.setSessionData(sessionId, response.sessionData)
                        sessionResult = response.sessionResult.orEmpty()
                        // TODO - Check if we need to support partial payment flow
                        when {
                            response.action != null -> SubmitResult.Action(response.action)
                            else -> SubmitResult.Completion(response.resultCode ?: RESULT_CODE_MISSING)
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
                        SubmitResult.Retry(error.message)
                    },
                )
            }
        }
    }

    private suspend fun handleBeforeSubmit(data: PaymentComponentData<*>): BeforeSubmitResult {
        val inputData = BeforeSubmitData(
            billingAddress = data.billingAddress,
            deliveryAddress = data.deliveryAddress,
            shopperName = data.shopperName,
            shopperEmail = data.shopperEmail,
        )

        return callbacks.onBeforeSubmit?.invoke(inputData) ?: BeforeSubmitResult.Proceed(inputData)
    }

    override suspend fun additionalDetails(data: ActionComponentData): AdditionalDetailsResult {
        sessionRepository.submitDetails(
            sessionId = sessionId,
            sessionData = sessionStateStore.getSessionData(checkoutSession),
            actionComponentData = data,
        ).fold(
            onSuccess = { response ->
                sessionStateStore.setSessionData(sessionId, response.sessionData)
                sessionResult = response.sessionResult.orEmpty()
                return AdditionalDetailsResult.Completion(response.resultCode ?: RESULT_CODE_MISSING)
            },
            onFailure = { error ->
                callbacks.onFailure(error.toCheckoutError())
                return AdditionalDetailsResult.Completion(CheckoutResultCode.ERROR.value)
            },
        )
    }

    override fun action(actionData: ActionData) {
        callbacks.onAction(actionData)
    }

    override fun complete(resultCode: CheckoutResultCode) {
        val isSessionResultExpectedToBeMissing = resultCode == CheckoutResultCode.CANCELLED ||
            resultCode == CheckoutResultCode.ERROR
        if (sessionId.isBlank()) {
            adyenLog(AdyenLogLevel.ERROR) { "Session completion called without a sessionId." }
        } else if (sessionResult.isBlank() && !isSessionResultExpectedToBeMissing) {
            adyenLog(AdyenLogLevel.ERROR) { "Session completion called without a sessionResult." }
        }

        val result = SessionCheckoutResult(
            resultCode = resultCode,
            sessionId = sessionId,
            sessionResult = sessionResult,
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
