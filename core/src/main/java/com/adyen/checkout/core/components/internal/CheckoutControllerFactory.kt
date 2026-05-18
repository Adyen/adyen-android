/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.AnalyticsManagerFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsSource
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.common.internal.CheckoutParamsFactory
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.components.AdvancedCheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.SessionCheckoutCallbacks
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository
import com.adyen.checkout.core.sessions.internal.data.api.SessionService
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupResponse
import kotlinx.coroutines.CoroutineScope

internal class CheckoutControllerFactory {

    @Suppress("LongMethod")
    fun create(
        target: CheckoutTarget,
        context: CheckoutContext,
        callbacks: CheckoutCallbacks,
        coroutineScope: CoroutineScope,
    ): CheckoutController {
        val checkoutConfiguration: CheckoutConfiguration
        val checkoutAttemptId: String?
        val publicKey: String?
        val session: CheckoutSession?

        when (context) {
            is CheckoutContext.Advanced -> {
                checkoutConfiguration = context.checkoutConfiguration
                checkoutAttemptId = context.checkoutAttemptId
                publicKey = context.publicKey
                session = null
            }

            is CheckoutContext.Sessions -> {
                checkoutConfiguration = context.checkoutConfiguration
                checkoutAttemptId = context.checkoutAttemptId
                publicKey = context.publicKey
                session = context.checkoutSession
            }
        }

        val checkoutParams = CheckoutParamsFactory().create(
            configuration = checkoutConfiguration,
            session = session,
            publicKey = publicKey,
        )

        val analyticsManager = createAnalyticsManager(
            params = checkoutParams,
            sessionId = session?.sessionSetupResponse?.id,
            checkoutAttemptId = checkoutAttemptId,
        )

        val componentRequestDispatcher = createComponentRequestDispatcher(
            callbacks = callbacks,
            sessionSetup = session?.sessionSetupResponse,
            params = checkoutParams,
        )

        val actionHandler = createActionHandler(
            componentRequestDispatcher = componentRequestDispatcher,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            params = checkoutParams,
        )

        val flow = createFlow(
            target = target,
            context = context,
            callbacks = callbacks,
            componentRequestDispatcher = componentRequestDispatcher,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            params = checkoutParams,
            actionHandler = actionHandler,
        )

        return CheckoutController(
            flow = flow,
        )
    }

    private fun createAnalyticsManager(
        params: CheckoutParams,
        sessionId: String?,
        checkoutAttemptId: String?,
    ) = AnalyticsManagerFactory().provide(
        params = params,
        // TODO - Analytics: Pass the correct paymentMethod type
        source = AnalyticsSource.PaymentComponent("paymentMethod.type"),
        sessionId = sessionId,
        checkoutAttemptId = checkoutAttemptId,
    )

    private fun createComponentRequestDispatcher(
        callbacks: CheckoutCallbacks,
        sessionSetup: SessionSetupResponse?,
        params: CheckoutParams,
    ): ComponentRequestDispatcher {
        return when (callbacks) {
            is AdvancedCheckoutCallbacks -> {
                AdvancedComponentRequestDispatcher(callbacks)
            }

            is SessionCheckoutCallbacks -> {
                requireNotNull(sessionSetup)
                val httpClient = HttpClientFactory.getHttpClient(params.environment)
                val sessionService = SessionService(httpClient)
                val sessionRepository = SessionRepository(sessionService, params.clientKey)
                SessionComponentRequestDispatcher(
                    initialSessionData = sessionSetup.sessionData,
                    sessionId = sessionSetup.id,
                    callbacks = callbacks,
                    sessionRepository = sessionRepository,
                )
            }

            else -> error("Unsupported callbacks: $callbacks")
        }
    }

    private fun createActionHandler(
        componentRequestDispatcher: ComponentRequestDispatcher,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        params: CheckoutParams,
    ) = ActionHandler(
        componentRequestDispatcher = componentRequestDispatcher,
        coroutineScope = coroutineScope,
        analyticsManager = analyticsManager,
        params = params,
    )

    @Suppress("LongParameterList")
    private fun createFlow(
        target: CheckoutTarget,
        context: CheckoutContext,
        callbacks: CheckoutCallbacks,
        componentRequestDispatcher: ComponentRequestDispatcher,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        params: CheckoutParams,
        actionHandler: ActionHandler,
    ): CheckoutFlow = when (target) {
        is CheckoutTarget.PaymentMethod,
        is CheckoutTarget.StoredPaymentMethod -> FullCheckoutFlow(
            target = target,
            context = context,
            callbacks = callbacks,
            componentRequestDispatcher = componentRequestDispatcher,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            params = params,
            actionHandler = actionHandler,
        )

        is CheckoutTarget.Action -> ActionOnlyCheckoutFlow(
            action = target.action,
            actionHandler = actionHandler,
        )

        else -> error("Unsupported target: $target")
    }
}
