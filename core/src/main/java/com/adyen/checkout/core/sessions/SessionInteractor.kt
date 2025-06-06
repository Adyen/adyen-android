/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.sessions

import com.adyen.checkout.core.paymentmethod.PaymentComponentState
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository
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
    ) {
        sessionRepository.submitPayment(
            sessionModel = sessionModel,
            paymentComponentData = paymentComponentState.data
        ).fold(
            onSuccess = { response ->
                updateSessionData(response.sessionData)

                // TODO - Handle API Call Result. Move [SessionsCallResult] here.
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
            }
        )
    }

    private fun updateSessionData(sessionData: String) {
        // TODO - Adyen logger
//        adyenLog(AdyenLogLevel.VERBOSE) { "Updating session data - $sessionData" }
        sessionSavedStateHandleContainer.updateSessionData(sessionData)
    }
}
