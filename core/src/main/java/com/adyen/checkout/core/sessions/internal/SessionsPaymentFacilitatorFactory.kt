/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/5/2025.
 */

package com.adyen.checkout.core.sessions.internal

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.internal.ActionProvider
import com.adyen.checkout.core.analytics.internal.AnalyticsManagerFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsSource
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.common.internal.helper.getLocale
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.data.model.PaymentMethodResponse
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

@Suppress("LongParameterList")
internal class SessionsPaymentFacilitatorFactory(
    private val applicationContext: Context,
    private val checkoutSession: CheckoutSession,
    private val checkoutConfiguration: CheckoutConfiguration,
    private val checkoutCallbacks: CheckoutCallbacks,
    private val savedStateHandle: SavedStateHandle,
    private val checkoutController: CheckoutController,
    private val publicKey: String?,
    private val checkoutAttemptId: String?,
) : PaymentFacilitatorFactory {

    override fun create(
        paymentMethod: PaymentMethodResponse,
        coroutineScope: CoroutineScope,
    ): PaymentFacilitator {
        val sessionSavedStateHandleContainer = SessionSavedStateHandleContainer(
            savedStateHandle = savedStateHandle,
            checkoutSession = checkoutSession,
        )

        val componentParamsBundle = CommonComponentParamsMapper().mapToParams(
            checkoutConfiguration = checkoutConfiguration,
            deviceLocale = applicationContext.getLocale(),
            dropInOverrideParams = null,
            componentSessionParams = SessionParamsFactory.create(checkoutSession),
            publicKey = publicKey,
        )

        val analyticsManager = AnalyticsManagerFactory().provide(
            componentParams = componentParamsBundle.commonComponentParams,
            applicationContext = applicationContext,
            source = AnalyticsSource.PaymentComponent(paymentMethod.type),
            sessionId = checkoutSession.sessionSetupResponse.id,
            checkoutAttemptId = checkoutAttemptId,
        )

        val paymentComponent = PaymentMethodProvider.get(
            application = applicationContext.applicationContext as android.app.Application,
            paymentMethod = paymentMethod,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            checkoutConfiguration = checkoutConfiguration,
            componentParamsBundle = componentParamsBundle,
            checkoutCallbacks = checkoutCallbacks,
        )

        val componentEventHandler = createComponentEventHandler(sessionSavedStateHandleContainer, analyticsManager)

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
            commonComponentParams = componentParamsBundle.commonComponentParams,
        )
    }

    private fun createComponentEventHandler(
        sessionSavedStateHandleContainer: SessionSavedStateHandleContainer,
        analyticsManager: com.adyen.checkout.core.analytics.internal.AnalyticsManager,
    ): SessionsComponentEventHandler<BasePaymentComponentState> {
        val sessionInteractor = SessionInteractor(
            sessionRepository = SessionRepository(
                sessionService = SessionService(
                    httpClient = HttpClientFactory.getHttpClient(checkoutConfiguration.environment),
                ),
                clientKey = checkoutConfiguration.clientKey,
            ),
            sessionSavedStateHandleContainer = sessionSavedStateHandleContainer,
            analyticsManager = analyticsManager,
            sessionResponse = sessionSavedStateHandleContainer.getSessionResponse(),
            isFlowTakenOver = sessionSavedStateHandleContainer.isFlowTakenOver ?: false,
        )

        return SessionsComponentEventHandler(
            sessionInteractor = sessionInteractor,
            componentCallbacks = checkoutCallbacks.toSessionsComponentCallbacks(),
        )
    }
}
