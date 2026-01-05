/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/6/2025.
 */

package com.adyen.checkout.core.components.internal

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.AnalyticsManagerFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsSource
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.common.internal.helper.getLocale
import com.adyen.checkout.core.components.AdyenPaymentFlowKey
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.internal.SessionInteractor
import com.adyen.checkout.core.sessions.internal.SessionSavedStateHandleContainer
import com.adyen.checkout.core.sessions.internal.SessionsComponentEventHandler
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository
import com.adyen.checkout.core.sessions.internal.data.api.SessionService
import com.adyen.checkout.core.sessions.internal.model.SessionParamsFactory
import kotlinx.coroutines.CoroutineScope

internal class PaymentFacilitatorProvider(
    private val commonComponentParamsMapper: CommonComponentParamsMapper = CommonComponentParamsMapper(),
    private val analyticsManagerFactory: AnalyticsManagerFactory = AnalyticsManagerFactory(),
    private val paymentFlowStrategyProvider: PaymentFlowStrategyProvider = PaymentFlowStrategyProvider(),
) {

    @Suppress("LongParameterList")
    fun provide(
        key: AdyenPaymentFlowKey,
        checkoutContext: CheckoutContext,
        checkoutCallbacks: CheckoutCallbacks,
        checkoutController: CheckoutController,
        applicationContext: Context,
        coroutineScope: CoroutineScope,
        savedStateHandle: SavedStateHandle,
    ): PaymentFacilitator {
        @Suppress("DestructuringDeclarationWithTooManyEntries")
        val contextData = resolveContextData(checkoutContext)

        val componentParamsBundle = commonComponentParamsMapper.mapToParams(
            checkoutConfiguration = contextData.checkoutConfiguration,
            deviceLocale = applicationContext.getLocale(),
            dropInOverrideParams = null,
            componentSessionParams = contextData.checkoutSession?.let { SessionParamsFactory.create(it) },
            publicKey = contextData.publicKey,
        )

        val analyticsManager = analyticsManagerFactory.provide(
            componentParams = componentParamsBundle.commonComponentParams,
            applicationContext = applicationContext,
            // TODO - Analytics. Provide payment method type to source
            source = AnalyticsSource.PaymentComponent("AwaitAction"),
            sessionId = contextData.checkoutSession?.sessionSetupResponse?.id,
            checkoutAttemptId = contextData.checkoutAttemptId,
        )

        val componentEventHandler = createComponentEventHandler(
            checkoutContext = checkoutContext,
            checkoutCallbacks = checkoutCallbacks,
            savedStateHandle = savedStateHandle,
            checkoutConfiguration = contextData.checkoutConfiguration,
            analyticsManager = analyticsManager,
        )

        val paymentFlowStrategy = paymentFlowStrategyProvider.get(
            key = key,
            paymentMethodsApiResponse = contextData.paymentMethodsApiResponse,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            checkoutConfiguration = contextData.checkoutConfiguration,
            componentParamsBundle = componentParamsBundle,
            checkoutCallbacks = checkoutCallbacks,
            savedStateHandle = savedStateHandle,
            componentEventHandler = componentEventHandler,
        )

        return PaymentFacilitator(
            paymentFlowStrategy = paymentFlowStrategy,
            coroutineScope = coroutineScope,
            checkoutController = checkoutController,
            commonComponentParams = componentParamsBundle.commonComponentParams,
        )
    }

    private fun resolveContextData(checkoutContext: CheckoutContext): ContextData {
        return when (checkoutContext) {
            is CheckoutContext.Advanced -> ContextData(
                checkoutConfiguration = checkoutContext.checkoutConfiguration,
                checkoutSession = null,
                publicKey = checkoutContext.publicKey,
                paymentMethodsApiResponse = checkoutContext.paymentMethodsApiResponse,
                checkoutAttemptId = checkoutContext.checkoutAttemptId,
            )

            is CheckoutContext.Sessions -> ContextData(
                checkoutConfiguration = checkoutContext.checkoutConfiguration,
                checkoutSession = checkoutContext.checkoutSession,
                publicKey = checkoutContext.publicKey,
                // TODO - Make sure paymentMethodsApiResponse is not null during deserialization
                paymentMethodsApiResponse = requireNotNull(
                    checkoutContext.checkoutSession.sessionSetupResponse.paymentMethodsApiResponse,
                ),
                checkoutAttemptId = checkoutContext.checkoutAttemptId,
            )
        }
    }

    private fun createComponentEventHandler(
        checkoutContext: CheckoutContext,
        checkoutCallbacks: CheckoutCallbacks,
        savedStateHandle: SavedStateHandle,
        checkoutConfiguration: CheckoutConfiguration,
        analyticsManager: AnalyticsManager,
    ): ComponentEventHandler<BasePaymentComponentState> {
        return when (checkoutContext) {
            is CheckoutContext.Advanced -> createAdvancedComponentEventHandler(checkoutCallbacks)
            is CheckoutContext.Sessions -> createSessionsComponentEventHandler(
                savedStateHandle = savedStateHandle,
                checkoutSession = checkoutContext.checkoutSession,
                checkoutConfiguration = checkoutConfiguration,
                checkoutCallbacks = checkoutCallbacks,
                analyticsManager = analyticsManager,
            )
        }
    }

    private fun createAdvancedComponentEventHandler(
        checkoutCallbacks: CheckoutCallbacks
    ): AdvancedComponentEventHandler<BasePaymentComponentState> {
        return AdvancedComponentEventHandler(
            componentCallbacks = checkoutCallbacks.toAdvancedComponentCallbacks(),
        )
    }

    private fun createSessionsComponentEventHandler(
        savedStateHandle: SavedStateHandle,
        checkoutSession: CheckoutSession,
        checkoutConfiguration: CheckoutConfiguration,
        checkoutCallbacks: CheckoutCallbacks,
        analyticsManager: AnalyticsManager,
    ): SessionsComponentEventHandler<BasePaymentComponentState> {
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
            analyticsManager = analyticsManager,
            sessionResponse = sessionSavedStateHandleContainer.getSessionResponse(),
            isFlowTakenOver = sessionSavedStateHandleContainer.isFlowTakenOver ?: false,
        )

        // TODO - Based on txVariant, needs to be abstracted away
        return SessionsComponentEventHandler(
            sessionInteractor = sessionInteractor,
            componentCallbacks = checkoutCallbacks.toSessionsComponentCallbacks(),
        )
    }

    private data class ContextData(
        val checkoutConfiguration: CheckoutConfiguration,
        val checkoutSession: CheckoutSession?,
        val publicKey: String?,
        val paymentMethodsApiResponse: PaymentMethodsApiResponse,
        val checkoutAttemptId: String?,
    )
}
