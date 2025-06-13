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
import com.adyen.checkout.core.internal.ui.model.ButtonComponentParamsMapper
import com.adyen.checkout.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.internal.ui.model.SessionParamsFactory
import com.adyen.checkout.core.mbway.internal.ui.getMBWayConfiguration
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.SessionInteractor
import com.adyen.checkout.core.sessions.SessionSavedStateHandleContainer
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository
import com.adyen.checkout.core.sessions.internal.data.api.SessionService
import kotlinx.coroutines.CoroutineScope
import java.util.Locale

internal class SessionsPaymentFacilitatorFactory(
    private val checkoutSession: CheckoutSession,
    private val checkoutConfiguration: CheckoutConfiguration,
    private val checkoutCallback: CheckoutCallback,
    private val savedStateHandle: SavedStateHandle
) : PaymentFacilitatorFactory {

    override fun create(
        coroutineScope: CoroutineScope,
    ): PaymentFacilitator {
        val componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
            checkoutConfiguration = checkoutConfiguration,

            // TODO - Add locale support, For now it's hardcoded to US
//        deviceLocale = localeProvider.getLocale(application)
            deviceLocale = Locale.US,
            dropInOverrideParams = null,
            componentSessionParams = SessionParamsFactory.create(checkoutSession),
            componentConfiguration = checkoutConfiguration.getMBWayConfiguration(),
        )

        val sessionSavedStateHandleContainer = SessionSavedStateHandleContainer(
            savedStateHandle = savedStateHandle,
            checkoutSession = checkoutSession,
        )

        return PaymentFacilitator(
            coroutineScope = coroutineScope,
            checkoutCallback = checkoutCallback,

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
            componentParams = componentParams,
        )
    }
}
