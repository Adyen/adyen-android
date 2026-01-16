/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.sessions.internal

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.ErrorEvent
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState
import com.adyen.checkout.core.sessions.SessionPaymentResult
import com.adyen.checkout.core.sessions.SessionResponse
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository
import com.adyen.checkout.core.sessions.internal.data.model.SessionDetailsResponse
import com.adyen.checkout.core.sessions.internal.data.model.SessionPaymentsResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// TODO - Add tests
internal class SessionInteractor(
    private val sessionRepository: SessionRepository,
    private val sessionSavedStateHandleContainer: SessionSavedStateHandleContainer,
    private val analyticsManager: AnalyticsManager,
    sessionResponse: SessionResponse,

    // TODO - Taken Over Flow
    @Suppress("UnusedPrivateProperty")
    isFlowTakenOver: Boolean,
) {

    private val _sessionFlow = MutableStateFlow(sessionResponse)
    val sessionFlow: Flow<SessionResponse> = _sessionFlow

    private val sessionResponse: SessionResponse get() = _sessionFlow.value

    suspend fun submitPayment(
        paymentComponentState: PaymentComponentState<*>,
    ) = sessionRepository.submitPayment(
        sessionResponse = sessionResponse,
        paymentComponentData = paymentComponentState.data,
    ).fold(
        onSuccess = { response ->
            updateSessionData(response.sessionData)

            when (val action = response.action) {
                null -> SessionCallResult.Payments.Finished(response.mapToSessionPaymentResult())
                else -> SessionCallResult.Payments.Action(action)
            }
        },
        onFailure = {
            paymentComponentState.data.paymentMethod?.type?.let { paymentMethodType ->
                val event = GenericEvents.error(
                    component = paymentMethodType,
                    event = ErrorEvent.API_PAYMENTS,
                )
                analyticsManager.trackEvent(event)
            }

            SessionCallResult.Payments.Error(throwable = it)
        },
    )

    suspend fun submitDetails(actionComponentData: ActionComponentData) =
        sessionRepository.submitDetails(sessionResponse, actionComponentData)
            .fold(
                onSuccess = { response ->
                    updateSessionData(response.sessionData)

                    when (val action = response.action) {
                        null -> SessionCallResult.Details.Finished(response.mapToSessionPaymentResult())
                        else -> SessionCallResult.Details.Action(action)
                    }
                },
                onFailure = {
                    SessionCallResult.Details.Error(throwable = it)
                },
            )

    private fun updateSessionData(sessionData: String) {
        adyenLog(AdyenLogLevel.VERBOSE) { "Updating session data - $sessionData" }
        sessionSavedStateHandleContainer.updateSessionData(sessionData)
    }

    private fun SessionPaymentsResponse.mapToSessionPaymentResult() = SessionPaymentResult(
        sessionId = sessionResponse.id,
        sessionResult = sessionResult,
        sessionData = sessionData,
        resultCode = resultCode,
        order = order,
    )

    private fun SessionDetailsResponse.mapToSessionPaymentResult() = SessionPaymentResult(
        sessionId = sessionResponse.id,
        sessionResult = sessionResult,
        sessionData = sessionData,
        resultCode = resultCode,
        order = order,
    )
}
