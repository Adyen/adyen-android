/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/5/2025.
 */

package com.adyen.checkout.core.sessions.internal

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.internal.ActionProvider
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.internal.BasePaymentComponentState
import com.adyen.checkout.core.components.internal.PaymentFacilitator
import com.adyen.checkout.core.components.internal.PaymentFacilitatorFactory
import com.adyen.checkout.core.components.internal.PaymentMethodProvider
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository
import com.adyen.checkout.core.sessions.internal.data.api.SessionService
import com.adyen.checkout.core.sessions.internal.model.SessionParamsFactory
import kotlinx.coroutines.CoroutineScope

internal class SessionsPaymentFacilitatorFactory(
    private val checkoutSession: CheckoutSession,
    private val checkoutConfiguration: CheckoutConfiguration,
    private val checkoutCallbacks: CheckoutCallbacks?,
    private val savedStateHandle: SavedStateHandle,
    private val checkoutController: CheckoutController,
) : PaymentFacilitatorFactory {

    override fun create(
        txVariant: String,
        coroutineScope: CoroutineScope,
    ): PaymentFacilitator {
        val sessionSavedStateHandleContainer = SessionSavedStateHandleContainer(
            savedStateHandle = savedStateHandle,
            checkoutSession = checkoutSession,
        )
        val sessionInteractor = SessionInteractor(
            sessionRepository = SessionRepository(
                sessionService = SessionService(
                    httpClient = HttpClientFactory.getHttpClient(checkoutConfiguration.environment),
                ),
                clientKey = checkoutConfiguration.clientKey,
            ),
            sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
            sessionModel = sessionSavedStateHandleContainer.getSessionModel(),
            isFlowTakenOver = sessionSavedStateHandleContainer.isFlowTakenOver ?: false,
        )

        // TODO - Based on txVariant, needs to be abstracted away
        val componentEventHandler =
            SessionsComponentEventHandler<BasePaymentComponentState>(
                sessionInteractor = sessionInteractor,
                checkoutCallbacks = checkoutCallbacks,
            )

        val paymentComponent = PaymentMethodProvider.get(
            txVariant = txVariant,
            coroutineScope = coroutineScope,
            checkoutConfiguration = checkoutConfiguration,
            componentSessionParams = SessionParamsFactory.create(checkoutSession),
        )

        val actionProvider = ActionProvider(
            checkoutConfiguration = checkoutConfiguration,
            savedStateHandle = savedStateHandle,
        )

        return PaymentFacilitator(
            paymentComponent = paymentComponent,
            coroutineScope = coroutineScope,
            componentEventHandler = componentEventHandler,
            actionProvider = actionProvider,
            checkoutController = checkoutController,
        )
    }
}
