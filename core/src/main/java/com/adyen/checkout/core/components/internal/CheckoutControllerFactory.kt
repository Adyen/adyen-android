/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2026.
 */

package com.adyen.checkout.core.components.internal

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.AnalyticsManagerFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsSource
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.components.AdvancedCheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.SessionCheckoutCallbacks
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository
import com.adyen.checkout.core.sessions.internal.data.api.SessionService
import com.adyen.checkout.core.sessions.internal.model.SessionParams
import com.adyen.checkout.core.sessions.internal.model.SessionParamsFactory
import kotlinx.coroutines.CoroutineScope
import java.util.Locale

internal class CheckoutControllerFactory {

    fun create(
        target: CheckoutTarget,
        context: CheckoutContext,
        callbacks: CheckoutCallbacks,
        // TODO - find a way to not require application contex
        applicationContext: Context,
        coroutineScope: CoroutineScope,
    ): CheckoutController {
        val checkoutConfiguration: CheckoutConfiguration
        val checkoutAttemptId: String?
        val publicKey: String?
        val componentSessionParams: SessionParams?
        val sessionId: String?

        when (context) {
            is CheckoutContext.Advanced -> {
                checkoutConfiguration = context.checkoutConfiguration
                checkoutAttemptId = context.checkoutAttemptId
                publicKey = context.publicKey
                componentSessionParams = null
                sessionId = null
            }

            is CheckoutContext.Sessions -> {
                checkoutConfiguration = context.checkoutConfiguration
                checkoutAttemptId = context.checkoutAttemptId
                publicKey = context.publicKey
                componentSessionParams = SessionParamsFactory.create(context.checkoutSession)
                sessionId = context.checkoutSession.sessionSetupResponse.id
            }
        }

        val componentParamsBundle = createComponentParamsBundle(
            checkoutConfiguration = checkoutConfiguration,
            sessionParams = componentSessionParams,
            publicKey = publicKey,
        )

        val analyticsManager = createAnalyticsManager(
            applicationContext = applicationContext,
            componentParamsBundle = componentParamsBundle,
            sessionId = sessionId,
            checkoutAttemptId = checkoutAttemptId,
        )

        val componentRequestDispatcher = createComponentRequestDispatcher(
            callbacks = callbacks,
            context = context,
            configuration = checkoutConfiguration,
        )

        val actionHandler = createActionHandler(
            componentRequestDispatcher = componentRequestDispatcher,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            checkoutConfiguration = checkoutConfiguration,
            componentParamsBundle = componentParamsBundle,
        )

        val flow = createFlow(
            target = target,
            context = context,
            componentRequestDispatcher = componentRequestDispatcher,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            checkoutConfiguration = checkoutConfiguration,
            componentParamsBundle = componentParamsBundle,
            actionHandler = actionHandler,
        )

        return CheckoutController(
            flow = flow,
        )
    }

    private fun createComponentParamsBundle(
        checkoutConfiguration: CheckoutConfiguration,
        sessionParams: SessionParams?,
        publicKey: String?,
    ) =
        CommonComponentParamsMapper().mapToParams(
            checkoutConfiguration = checkoutConfiguration,
            deviceLocale = AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault(),
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
            publicKey = publicKey,
        )

    private fun createAnalyticsManager(
        applicationContext: Context,
        componentParamsBundle: ComponentParamsBundle,
        sessionId: String?,
        checkoutAttemptId: String?,
    ) = AnalyticsManagerFactory().provide(
        componentParams = componentParamsBundle.commonComponentParams,
        applicationContext = applicationContext,
        // TODO - Analytics: Pass the correct paymentMethod type
        source = AnalyticsSource.PaymentComponent("paymentMethod.type"),
        sessionId = sessionId,
        checkoutAttemptId = checkoutAttemptId,
    )

    private fun createComponentRequestDispatcher(
        callbacks: CheckoutCallbacks,
        context: CheckoutContext,
        configuration: CheckoutConfiguration,
    ): ComponentRequestDispatcher {
        return when (callbacks) {
            is AdvancedCheckoutCallbacks -> {
                AdvancedComponentRequestDispatcher(callbacks)
            }

            is SessionCheckoutCallbacks -> {
                val session = (context as CheckoutContext.Sessions).checkoutSession.sessionSetupResponse
                val httpClient = HttpClientFactory.getHttpClient(configuration.environment)
                val sessionService = SessionService(httpClient)
                val sessionRepository = SessionRepository(sessionService, configuration.clientKey)
                SessionComponentRequestDispatcher(
                    initialSessionData = session.sessionData,
                    sessionId = session.id,
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
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
    ) = ActionHandler(
        componentRequestDispatcher = componentRequestDispatcher,
        coroutineScope = coroutineScope,
        analyticsManager = analyticsManager,
        checkoutConfiguration = checkoutConfiguration,
        componentParamsBundle = componentParamsBundle,
    )

    @Suppress("LongParameterList")
    private fun createFlow(
        target: CheckoutTarget,
        context: CheckoutContext,
        componentRequestDispatcher: ComponentRequestDispatcher,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
        actionHandler: ActionHandler,
    ): CheckoutFlow = when (target) {
        is CheckoutTarget.PaymentMethod,
        is CheckoutTarget.StoredPaymentMethod -> FullCheckoutFlow(
            target = target,
            context = context,
            componentRequestDispatcher = componentRequestDispatcher,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            checkoutConfiguration = checkoutConfiguration,
            componentParamsBundle = componentParamsBundle,
            actionHandler = actionHandler,
        )

        is CheckoutTarget.Action -> ActionOnlyCheckoutFlow(
            action = target.action,
            actionHandler = actionHandler,
        )

        else -> error("Unsupported target: $target")
    }
}
