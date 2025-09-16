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
import com.adyen.checkout.core.analytics.internal.AnalyticsManagerFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsSource
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.internal.BasePaymentComponentState
import com.adyen.checkout.core.components.internal.PaymentFacilitator
import com.adyen.checkout.core.components.internal.PaymentFacilitatorFactory
import com.adyen.checkout.core.components.internal.PaymentMethodProvider
import com.adyen.checkout.core.components.internal.toSessionsComponentCallbacks
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository
import com.adyen.checkout.core.sessions.internal.data.api.SessionService
import com.adyen.checkout.core.sessions.internal.model.SessionParamsFactory
import kotlinx.coroutines.CoroutineScope
import java.util.Locale

internal class SessionsPaymentFacilitatorFactory(
    private val checkoutSession: CheckoutSession,
    private val checkoutConfiguration: CheckoutConfiguration,
    private val checkoutCallbacks: CheckoutCallbacks,
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
                componentCallbacks = checkoutCallbacks.toSessionsComponentCallbacks(),
            )

        val componentParamsBundle = CommonComponentParamsMapper().mapToParams(
            checkoutConfiguration = checkoutConfiguration,

            // TODO - Add locale support, For now it's hardcoded to US
            // deviceLocale = localeProvider.getLocale(application)
            deviceLocale = Locale.US,
            dropInOverrideParams = null,
            componentSessionParams = SessionParamsFactory.create(checkoutSession),
        )

        // TODO - Analytics. We might need to change the logic on AnalyticsManager creation.
        val analyticsManager = AnalyticsManagerFactory().provide(
            componentParams = componentParamsBundle.commonComponentParams,
            application = null,
            // TODO - Analytics. Provide payment method type to source
            source = AnalyticsSource.PaymentComponent("AwaitAction"),
            sessionId = checkoutSession.sessionSetupResponse.id,
        )

        val paymentComponent = PaymentMethodProvider.get(
            txVariant = txVariant,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            checkoutConfiguration = checkoutConfiguration,
            componentParamsBundle = componentParamsBundle,
        )

        val actionProvider = ActionProvider(
            analyticsManager = analyticsManager,
            checkoutConfiguration = checkoutConfiguration,
            savedStateHandle = savedStateHandle,
            commonComponentParams = componentParamsBundle.commonComponentParams,
        )

        return PaymentFacilitator(
            paymentComponent = paymentComponent,
            coroutineScope = coroutineScope,
            componentEventHandler = componentEventHandler,
            actionProvider = actionProvider,
            checkoutController = checkoutController,
            shopperLocale = componentParamsBundle.commonComponentParams.shopperLocale,
        )
    }
}
