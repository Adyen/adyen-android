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
import com.adyen.checkout.core.sessions.SessionModel
import com.adyen.checkout.core.sessions.SessionPaymentResult
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
    sessionModel: SessionModel,

    // TODO - Taken Over Flow
    @Suppress("UnusedPrivateProperty")
    isFlowTakenOver: Boolean,
) {

    private val _sessionFlow = MutableStateFlow(sessionModel)
    val sessionFlow: Flow<SessionModel> = _sessionFlow

    private val sessionModel: SessionModel get() = _sessionFlow.value

    suspend fun submitPayment(
        paymentComponentState: PaymentComponentState<*>,
    ) = sessionRepository.submitPayment(
        sessionModel = sessionModel,
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
        sessionRepository.submitDetails(sessionModel, actionComponentData)
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
        sessionId = sessionModel.id,
        sessionResult = sessionResult,
        sessionData = sessionData,
        resultCode = resultCode,
        order = order,
    )

    private fun SessionDetailsResponse.mapToSessionPaymentResult() = SessionPaymentResult(
        sessionId = sessionModel.id,
        sessionResult = sessionResult,
        sessionData = sessionData,
        resultCode = resultCode,
        order = order,
    )
}
