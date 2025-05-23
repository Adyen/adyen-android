/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.core.AdyenCheckout
import com.adyen.checkout.core.internal.data.api.HttpClientFactory
import com.adyen.checkout.core.sessions.SessionInteractor
import com.adyen.checkout.core.sessions.SessionSavedStateHandleContainer
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository
import com.adyen.checkout.core.sessions.internal.data.api.SessionService

internal class AdyenComponent(adyenCheckout: AdyenCheckout, savedStateHandle: SavedStateHandle) : ViewModel() {

    private val paymentFacilitator: PaymentFacilitator

    init {
        // TODO - Initialize Payment Flow
        if (adyenCheckout.checkoutSession != null) {
            val sessionSavedStateHandleContainer = SessionSavedStateHandleContainer(
                savedStateHandle = savedStateHandle,

                // TODO - Advanced Flow
                checkoutSession = adyenCheckout.checkoutSession
            )

            paymentFacilitator = PaymentFacilitator(
                coroutineScope = viewModelScope,
                adyenCheckout = adyenCheckout,

                // TODO - Where should we initialize sessions interactor?
                sessionInteractor = SessionInteractor(
                    sessionRepository = SessionRepository(
                        sessionService = SessionService(
                            httpClient = HttpClientFactory.getHttpClient(
                                adyenCheckout.checkoutConfiguration.environment
                            ),
                        ),
                        clientKey = adyenCheckout.checkoutConfiguration.clientKey,
                    ),
                    sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
                    sessionModel = sessionSavedStateHandleContainer.getSessionModel(),
                    isFlowTakenOver = sessionSavedStateHandleContainer.isFlowTakenOver ?: false,
                ),
            )
        } else {
            // TODO - Advanced Flow
            paymentFacilitator = TODO("Not yet implemented")
        }
    }

    @Composable
    internal fun ViewFactory(modifier: Modifier = Modifier) {
        paymentFacilitator.ViewFactory(modifier)
    }

    fun observe(lifecycle: Lifecycle) {
        paymentFacilitator.observe(lifecycle)
    }

    fun submit() {
        paymentFacilitator.submit()
    }
}
