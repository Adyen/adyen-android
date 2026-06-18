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
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository
import com.adyen.checkout.core.sessions.internal.data.api.SessionService
import kotlinx.coroutines.CoroutineScope

@Suppress("TooManyFunctions")
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
        val (checkoutParams, analyticsManager) = createCommonDependencies(context)
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
        val (checkoutParams, analyticsManager) = createCommonDependencies(context)
        val actionHandler = createActionHandler(
            componentRequestDispatcher = componentRequestDispatcher,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            params = checkoutParams,
        )

        val paymentComponentResult = createPaymentComponent(
            target = target,
            context = context,
            callbacks = callbacks,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            checkoutParams = checkoutParams,
        )

        val paymentComponent = when (paymentComponentResult) {
            is PaymentComponentResult.Success -> paymentComponentResult.component
            is PaymentComponentResult.Failure -> {
                componentRequestDispatcher.failure(
                    CheckoutError(
                        code = CheckoutError.ErrorCode.PAYMENT_METHOD_FAILURE,
                        message = paymentComponentResult.message,
                    ),
                )
                null
            }
        }

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
    ): PaymentComponentResult {
        return when (target) {
            is CheckoutTarget.PaymentMethod -> createPaymentComponent(
                target = target,
                context = context,
                callbacks = callbacks,
                coroutineScope = coroutineScope,
                analyticsManager = analyticsManager,
                checkoutParams = checkoutParams,
            )

            is CheckoutTarget.StoredPaymentMethod -> createStoredPaymentComponent(
                target = target,
                context = context,
                coroutineScope = coroutineScope,
                analyticsManager = analyticsManager,
                checkoutParams = checkoutParams,
            )

            else -> PaymentComponentResult.Failure("Unsupported checkout target.")
        }
    }

    @Suppress("LongParameterList", "ReturnCount")
    private fun createPaymentComponent(
        target: CheckoutTarget.PaymentMethod,
        context: CheckoutContext,
        callbacks: CheckoutCallbacks,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutParams: CheckoutParams,
    ): PaymentComponentResult {
        val paymentMethods = context.getPaymentMethodResponse()?.paymentMethods
            ?: return PaymentComponentResult.Failure("No payment methods response available.")

        val paymentMethod = paymentMethods.find { it.type == target.type }
            ?: return PaymentComponentResult.Failure(
                "Payment method '${target.type}' was not found in the payment methods response.",
            )

        val component = PaymentMethodProvider.getPaymentComponent(
            paymentMethod = paymentMethod,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            params = checkoutParams,
            additionalCallbacks = callbacks.additionalCallbacks,
        ) ?: return PaymentComponentResult.Failure(
            "Payment method '${target.type}' is not supported. " +
                "Ensure the corresponding module is included in your build dependencies.",
        )

        return PaymentComponentResult.Success(component)
    }

    @Suppress("ReturnCount")
    private fun createStoredPaymentComponent(
        target: CheckoutTarget.StoredPaymentMethod,
        context: CheckoutContext,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutParams: CheckoutParams,
    ): PaymentComponentResult {
        val storedPaymentMethods = context.getPaymentMethodResponse()?.storedPaymentMethods
            ?: return PaymentComponentResult.Failure("No payment methods response available.")

        val storedPaymentMethod = storedPaymentMethods.find { it.id == target.id }
            ?: return PaymentComponentResult.Failure(
                "Stored payment method with id '${target.id}' was not found in the payment methods response.",
            )

        val component = PaymentMethodProvider.getStoredPaymentComponent(
            storedPaymentMethod = storedPaymentMethod,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            params = checkoutParams,
        ) ?: return PaymentComponentResult.Failure(
            "Stored payment method type '${storedPaymentMethod.type}' is not supported. " +
                "Ensure the corresponding module is included in your build dependencies.",
        )

        return PaymentComponentResult.Success(component)
    }

    private fun CheckoutContext.getPaymentMethodResponse(): PaymentMethods? {
        return when (this) {
            is CheckoutContext.Advanced -> paymentMethods
            is CheckoutContext.Sessions -> checkoutSession.sessionSetupResponse.paymentMethods
            is CheckoutContext.ActionOnly -> null
        }
    }
}
