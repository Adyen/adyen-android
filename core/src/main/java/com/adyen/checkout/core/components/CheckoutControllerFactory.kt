package com.adyen.checkout.core.components

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.adyen.checkout.core.analytics.internal.AnalyticsManagerFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsSource
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParamsMapper
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

        val componentParamsBundle = CommonComponentParamsMapper().mapToParams(
            checkoutConfiguration = checkoutConfiguration,
            deviceLocale = AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault(),
            dropInOverrideParams = null,
            componentSessionParams = componentSessionParams,
            publicKey = publicKey,
        )

        val analyticsManager = AnalyticsManagerFactory().provide(
            componentParams = componentParamsBundle.commonComponentParams,
            applicationContext = applicationContext,
            // TODO - Analytics: Pass the correct paymentMethod type
            source = AnalyticsSource.PaymentComponent("paymentMethod.type"),
            sessionId = sessionId,
            checkoutAttemptId = checkoutAttemptId,
        )

        return CheckoutController(
            target = target,
            context = context,
            callbacks = callbacks,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            checkoutConfiguration = checkoutConfiguration,
            componentParamsBundle = componentParamsBundle,
        )
    }
}
