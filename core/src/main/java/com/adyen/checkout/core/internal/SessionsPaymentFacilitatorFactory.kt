/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/5/2025.
 */

package com.adyen.checkout.core.internal

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.CheckoutCallback
import com.adyen.checkout.core.CheckoutConfiguration
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.SessionInteractor
import com.adyen.checkout.core.sessions.SessionSavedStateHandleContainer
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository
import com.adyen.checkout.core.sessions.internal.data.api.SessionService
import kotlinx.coroutines.CoroutineScope

internal class SessionsPaymentFacilitatorFactory : PaymentFacilitatorFactory {
    fun create(
        checkoutSession: CheckoutSession,
        checkoutConfiguration: CheckoutConfiguration,
        checkoutCallback: CheckoutCallback,
        coroutineScope: CoroutineScope,
        savedStateHandle: SavedStateHandle
    ): PaymentFacilitator {
        val sessionSavedStateHandleContainer = SessionSavedStateHandleContainer(
            savedStateHandle = savedStateHandle,
            checkoutSession = checkoutSession,
        )
        // Sessions Flow
        return PaymentFacilitator(
            coroutineScope = coroutineScope,
            checkoutSession = checkoutSession,
            checkoutConfiguration = checkoutConfiguration,
            checkoutCallback = checkoutCallback,

            // TODO - Where should we initialize sessions interactor?
            sessionInteractor = SessionInteractor(
                sessionRepository = SessionRepository(
                    sessionService = SessionService(
                        httpClient = HttpClientFactory.getHttpClient(checkoutConfiguration.environment),
                    ),
                    clientKey = checkoutConfiguration.clientKey,
                ),
                sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
                sessionModel = sessionSavedStateHandleContainer.getSessionModel(),
                isFlowTakenOver = sessionSavedStateHandleContainer.isFlowTakenOver ?: false,
            ),
        )
    }
}
