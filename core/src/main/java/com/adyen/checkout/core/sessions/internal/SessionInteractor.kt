/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.sessions.internal

import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState
import com.adyen.checkout.core.sessions.SessionModel
import com.adyen.checkout.core.sessions.SessionPaymentResult
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository
import com.adyen.checkout.core.sessions.internal.data.model.SessionPaymentsResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class SessionInteractor(
    private val sessionRepository: SessionRepository,
    private val sessionSavedStateHandleContainer: SessionSavedStateHandleContainer,
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
    ): SessionCallResult.Payments {
        sessionRepository.submitPayment(
            sessionModel = sessionModel,
            paymentComponentData = paymentComponentState.data,
        ).fold(
            onSuccess = { response ->
                updateSessionData(response.sessionData)

                val action = response.action
                return when {
                    action != null -> SessionCallResult.Payments.Action(action)
                    else -> SessionCallResult.Payments.Finished(response.mapToSessionPaymentResult())
                }
            },
            onFailure = {
                paymentComponentState.data.paymentMethod?.type?.let { paymentMethodType ->
                    // TODO - Analytics track event
//                    val event = GenericEvents.error(
//                        component = paymentMethodType,
//                        event = ErrorEvent.API_PAYMENTS,
//                    )
//                    analyticsManager?.trackEvent(event)
                }

                return SessionCallResult.Payments.Error(throwable = it)
            },
        )
    }

    private fun updateSessionData(sessionData: String) {
        adyenLog(AdyenLogLevel.VERBOSE) { "Updating session data - $sessionData" }
        sessionSavedStateHandleContainer.updateSessionData(sessionData)
    }

    private fun SessionPaymentsResponse.mapToSessionPaymentResult() = SessionPaymentResult(
        sessionId = sessionModel.id,
        sessionResult = sessionResult,
        sessionData = sessionData,
        resultCode = resultCode,
    )
}
