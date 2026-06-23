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
import com.adyen.checkout.core.common.internal.checkoutAttemptId
import com.adyen.checkout.core.common.internal.publicKey
import com.adyen.checkout.core.components.ActionOnlyCheckoutCallbacks
import com.adyen.checkout.core.components.AdvancedCheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.SessionCheckoutCallbacks
import com.adyen.checkout.core.components.internal.data.provider.DefaultSdkDataProvider
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
        context: CheckoutContext.ActionOnly,
        callbacks: ActionOnlyCheckoutCallbacks,
        coroutineScope: CoroutineScope,
    ): CheckoutController {
        // TODO - what should be the payment method type for action only?
        val (checkoutParams, analyticsManager) = createCommonDependencies(
            paymentMethodType = "NONE",
            context = context,
            coroutineScope = coroutineScope,
        )
        val componentRequestDispatcher = ActionOnlyComponentRequestDispatcher(callbacks)
        val actionHandler = createActionHandler(
            componentRequestDispatcher = componentRequestDispatcher,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            params = checkoutParams,
        )
        val flow = ActionOnlyCheckoutFlow(
            action = context.action,
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
        val paymentMethod = PaymentMethodResolver.resolve(target, context) ?: return createFailedCheckoutController(
            errorMessage = "Payment method for target '$target' was not found in the payment methods response.",
            componentRequestDispatcher = componentRequestDispatcher,
        )

        val (checkoutParams, analyticsManager) = createCommonDependencies(
            paymentMethodType = paymentMethod.type,
            context = context,
            coroutineScope = coroutineScope,
        )
        val actionHandler = createActionHandler(
            componentRequestDispatcher = componentRequestDispatcher,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            params = checkoutParams,
        )
        val sdkDataProvider = DefaultSdkDataProvider(context.checkoutAttemptId)
        val paymentComponentResult = PaymentComponentResolver.resolve(
            paymentMethod = paymentMethod,
            callbacks = callbacks,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            sdkDataProvider = sdkDataProvider,
            checkoutParams = checkoutParams,
        )

        return when (paymentComponentResult) {
            is PaymentComponentResult.Success -> {
                val flow = FullCheckoutFlow(
                    componentRequestDispatcher = componentRequestDispatcher,
                    coroutineScope = coroutineScope,
                    paymentComponent = paymentComponentResult.component,
                    actionHandler = actionHandler,
                )

                CheckoutController(flow = flow)
            }

            is PaymentComponentResult.Failure -> {
                createFailedCheckoutController(
                    errorMessage = paymentComponentResult.message,
                    componentRequestDispatcher = componentRequestDispatcher,
                )
            }
        }
    }

    private fun createCommonDependencies(
        paymentMethodType: String,
        context: CheckoutContext,
        coroutineScope: CoroutineScope,
    ): CommonDependencies {
        val session = (context as? CheckoutContext.Sessions)?.checkoutSession

        val checkoutParams = CheckoutParamsFactory().create(
            configuration = context.checkoutConfiguration,
            session = session,
            publicKey = context.publicKey,
        )

        val analyticsManager = AnalyticsManagerFactory().provide(
            params = checkoutParams,
            source = AnalyticsSource.PaymentComponent(paymentMethodType),
            sessionId = session?.sessionSetupResponse?.id,
            checkoutAttemptId = context.checkoutAttemptId,
            coroutineScope = coroutineScope,
        )

        return CommonDependencies(
            checkoutParams = checkoutParams,
            analyticsManager = analyticsManager,
        )
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

    private fun createFailedCheckoutController(
        errorMessage: String,
        componentRequestDispatcher: ComponentRequestDispatcher,
    ): CheckoutController {
        val flow = FailureCheckoutFlow(
            errorMessage = errorMessage,
            componentRequestDispatcher = componentRequestDispatcher,
        )
        return CheckoutController(flow)
    }

    private data class CommonDependencies(
        val checkoutParams: CheckoutParams,
        val analyticsManager: AnalyticsManager,
    )
}
