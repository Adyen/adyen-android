/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.AnalyticsManagerFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsSource
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.common.internal.CheckoutParamsFactory
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.components.ActionOnlyCheckoutCallbacks
import com.adyen.checkout.core.components.AdvancedCheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.SessionCheckoutCallbacks
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository
import com.adyen.checkout.core.sessions.internal.data.api.SessionService
import kotlinx.coroutines.CoroutineScope

internal class CheckoutControllerFactory {

    @Suppress("LongMethod")
    fun create(
        target: CheckoutTarget,
        context: CheckoutContext.Advanced,
        callbacks: AdvancedCheckoutCallbacks,
        coroutineScope: CoroutineScope,
    ): CheckoutController {
        val checkoutConfiguration = context.checkoutConfiguration
        val checkoutAttemptId = context.checkoutAttemptId
        val publicKey = context.publicKey

        val checkoutParams = CheckoutParamsFactory().create(
            configuration = checkoutConfiguration,
            session = null,
            publicKey = publicKey,
        )

        val analyticsManager = createAnalyticsManager(
            params = checkoutParams,
            sessionId = null,
            checkoutAttemptId = checkoutAttemptId,
        )

        val componentRequestDispatcher = AdvancedComponentRequestDispatcher(callbacks)

        val actionHandler = createActionHandler(
            componentRequestDispatcher = componentRequestDispatcher,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            params = checkoutParams,
        )

        val flow = FullCheckoutFlow(
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

    @Suppress("LongMethod")
    fun create(
        target: CheckoutTarget,
        context: CheckoutContext.Sessions,
        callbacks: SessionCheckoutCallbacks,
        coroutineScope: CoroutineScope,
    ): CheckoutController {
        val checkoutConfiguration = context.checkoutConfiguration
        val checkoutAttemptId = context.checkoutAttemptId
        val publicKey = context.publicKey
        val session = context.checkoutSession

        val checkoutParams = CheckoutParamsFactory().create(
            configuration = checkoutConfiguration,
            session = session,
            publicKey = publicKey,
        )

        val analyticsManager = createAnalyticsManager(
            params = checkoutParams,
            sessionId = session.sessionSetupResponse.id,
            checkoutAttemptId = checkoutAttemptId,
        )

        val httpClient = HttpClientFactory.getHttpClient(checkoutConfiguration.environment)
        val sessionService = SessionService(httpClient)
        val sessionRepository = SessionRepository(sessionService, checkoutConfiguration.clientKey)
        val componentRequestDispatcher = SessionComponentRequestDispatcher(
            initialSessionData = session.sessionSetupResponse.sessionData,
            sessionId = session.sessionSetupResponse.id,
            callbacks = callbacks,
            sessionRepository = sessionRepository,
        )

        val actionHandler = createActionHandler(
            componentRequestDispatcher = componentRequestDispatcher,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            params = checkoutParams,
        )

        val flow = FullCheckoutFlow(
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

    fun create(
        action: Action,
        context: CheckoutContext.ActionOnly,
        callbacks: ActionOnlyCheckoutCallbacks,
        coroutineScope: CoroutineScope,
    ): CheckoutController {
        val checkoutConfiguration = context.checkoutConfiguration
        val checkoutAttemptId = context.checkoutAttemptId
        val publicKey = context.publicKey

        val checkoutParams = CheckoutParamsFactory().create(
            configuration = checkoutConfiguration,
            session = null,
            publicKey = publicKey,
        )

        val analyticsManager = createAnalyticsManager(
            params = checkoutParams,
            sessionId = null,
            checkoutAttemptId = checkoutAttemptId,
        )

        val componentRequestDispatcher = ActionOnlyComponentRequestDispatcher(callbacks)

        val actionHandler = createActionHandler(
            componentRequestDispatcher = componentRequestDispatcher,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            params = checkoutParams,
        )

        val flow = ActionOnlyCheckoutFlow(
            action = action,
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
}
