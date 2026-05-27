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
import com.adyen.checkout.core.common.internal.checkoutAttemptId
import com.adyen.checkout.core.common.internal.publicKey
import com.adyen.checkout.core.components.ActionOnlyCheckoutCallbacks
import com.adyen.checkout.core.components.AdvancedCheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.SessionCheckoutCallbacks
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository
import com.adyen.checkout.core.sessions.internal.data.api.SessionService
import kotlinx.coroutines.CoroutineScope

internal class CheckoutControllerFactory {

    fun create(
        target: CheckoutTarget,
        context: CheckoutContext.Advanced,
        callbacks: AdvancedCheckoutCallbacks,
        coroutineScope: CoroutineScope,
    ): CheckoutController {
        val componentRequestDispatcher = AdvancedComponentRequestDispatcher(callbacks)
        return createFullCheckoutController(
            target = target,
            context = context,
            callbacks = callbacks,
            componentRequestDispatcher = componentRequestDispatcher,
            coroutineScope = coroutineScope,
        )
    }

    fun create(
        target: CheckoutTarget,
        context: CheckoutContext.Sessions,
        callbacks: SessionCheckoutCallbacks,
        coroutineScope: CoroutineScope,
    ): CheckoutController {
        val componentRequestDispatcher = createSessionComponentRequestDispatcher(
            context = context,
            callbacks = callbacks,
        )
        return createFullCheckoutController(
            target = target,
            context = context,
            callbacks = callbacks,
            componentRequestDispatcher = componentRequestDispatcher,
            coroutineScope = coroutineScope,
        )
    }

    fun create(
        action: Action,
        context: CheckoutContext.ActionOnly,
        callbacks: ActionOnlyCheckoutCallbacks,
        coroutineScope: CoroutineScope,
    ): CheckoutController {
        val (checkoutParams, analyticsManager) = createCommonDependencies(context)
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
        return CheckoutController(flow = flow)
    }

    private fun createFullCheckoutController(
        target: CheckoutTarget,
        context: CheckoutContext,
        callbacks: CheckoutCallbacks,
        componentRequestDispatcher: SubmittableComponentRequestDispatcher,
        coroutineScope: CoroutineScope,
    ): CheckoutController {
        val (checkoutParams, analyticsManager) = createCommonDependencies(context)
        val actionHandler = createActionHandler(
            componentRequestDispatcher = componentRequestDispatcher,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            params = checkoutParams,
        )

        val paymentComponent = createPaymentComponent(
            target = target,
            context = context,
            callbacks = callbacks,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            checkoutParams = checkoutParams,
        )

        val flow = FullCheckoutFlow(
            componentRequestDispatcher = componentRequestDispatcher,
            coroutineScope = coroutineScope,
            paymentComponent = paymentComponent,
            actionHandler = actionHandler,
        )

        return CheckoutController(flow = flow)
    }

    private fun createCommonDependencies(context: CheckoutContext): CommonDependencies {
        val session = (context as? CheckoutContext.Sessions)?.checkoutSession

        val checkoutParams = CheckoutParamsFactory().create(
            configuration = context.checkoutConfiguration,
            session = session,
            publicKey = context.publicKey,
        )

        val analyticsManager = AnalyticsManagerFactory().provide(
            params = checkoutParams,
            // TODO - Analytics: Pass the correct paymentMethod type
            source = AnalyticsSource.PaymentComponent("paymentMethod.type"),
            sessionId = session?.sessionSetupResponse?.id,
            checkoutAttemptId = context.checkoutAttemptId,
        )

        return CommonDependencies(checkoutParams, analyticsManager)
    }

    private fun createSessionComponentRequestDispatcher(
        context: CheckoutContext.Sessions,
        callbacks: SessionCheckoutCallbacks,
    ): SessionComponentRequestDispatcher {
        val checkoutConfiguration = context.checkoutConfiguration
        val sessionSetup = context.checkoutSession.sessionSetupResponse
        val httpClient = HttpClientFactory.getHttpClient(checkoutConfiguration.environment)
        val sessionService = SessionService(httpClient)
        val sessionRepository = SessionRepository(sessionService, checkoutConfiguration.clientKey)
        return SessionComponentRequestDispatcher(
            initialSessionData = sessionSetup.sessionData,
            sessionId = sessionSetup.id,
            callbacks = callbacks,
            sessionRepository = sessionRepository,
        )
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

    private data class CommonDependencies(
        val checkoutParams: CheckoutParams,
        val analyticsManager: AnalyticsManager,
    )

    @Suppress("LongParameterList")
    private fun createPaymentComponent(
        target: CheckoutTarget,
        context: CheckoutContext,
        callbacks: CheckoutCallbacks,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutParams: CheckoutParams,
    ): PaymentComponent? {
        return when (target) {
            is CheckoutTarget.PaymentMethod -> {
                context.getPaymentMethodResponse()
                    ?.paymentMethods
                    ?.find { it.type == target.type }
                    ?.let { paymentMethod ->
                        PaymentMethodProvider.getPaymentComponent(
                            paymentMethod = paymentMethod,
                            coroutineScope = coroutineScope,
                            analyticsManager = analyticsManager,
                            params = checkoutParams,
                            additionalCallbacks = callbacks.additionalCallbacks,
                        )
                    }
            }

            is CheckoutTarget.StoredPaymentMethod -> {
                context.getPaymentMethodResponse()
                    ?.storedPaymentMethods
                    ?.find { it.id == target.id }
                    ?.let { storedPaymentMethod ->
                        PaymentMethodProvider.getStoredPaymentComponent(
                            storedPaymentMethod = storedPaymentMethod,
                            coroutineScope = coroutineScope,
                            analyticsManager = analyticsManager,
                            params = checkoutParams,
                        )
                    }
            }

            else -> null
        }
    }

    private fun CheckoutContext.getPaymentMethodResponse(): PaymentMethods? {
        return when (this) {
            is CheckoutContext.Advanced -> paymentMethods
            is CheckoutContext.Sessions -> checkoutSession.sessionSetupResponse.paymentMethods
            is CheckoutContext.ActionOnly -> null
        }
    }
}
