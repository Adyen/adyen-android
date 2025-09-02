/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.await.internal.ui

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.data.AwaitAction
import com.adyen.checkout.core.action.internal.ActionFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsManagerFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsSource
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.PaymentDataRepository
import com.adyen.checkout.core.components.internal.data.api.DefaultStatusRepository
import com.adyen.checkout.core.components.internal.data.api.StatusService
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.redirect.internal.DefaultRedirectHandler
import kotlinx.coroutines.CoroutineScope
import java.util.Locale

internal class AwaitFactory : ActionFactory<AwaitComponent> {

    @Suppress("TooGenericExceptionThrown")
    override fun create(
        action: Action,
        coroutineScope: CoroutineScope,
        checkoutConfiguration: CheckoutConfiguration,
        savedStateHandle: SavedStateHandle,
    ): AwaitComponent {
        if (action !is AwaitAction) {
//          TODO - Error Propagation
//          throw ComponentException("Unsupported action")
            throw RuntimeException("Unsupported action")
        }

        val componentParams = CommonComponentParamsMapper().mapToParams(
            checkoutConfiguration = checkoutConfiguration,
            // TODO - Add locale support, For now it's hardcoded to US
            // deviceLocale = localeProvider.getLocale(application)
            deviceLocale = Locale.US,
            dropInOverrideParams = null,
            componentSessionParams = null,
        ).commonComponentParams

        // TODO - Analytics. We might need to change the logic on AnalyticsManager creation.
        val analyticsManager = AnalyticsManagerFactory().provide(
            componentParams = componentParams,
            application = null,
            // TODO - Analytics. When we move the analyticsManager, the source can also be adjusted
            source = AnalyticsSource.PaymentComponent("AwaitAction"),
            // TODO - When we move out componentParams logic creation to the payment facilitator
            //  factory level, Analytics manager should move there too and sessionId can be passed
            sessionId = null,
        )

        val redirectHandler = DefaultRedirectHandler()
        val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
        val statusService = StatusService(httpClient)
        val statusRepository = DefaultStatusRepository(statusService, componentParams.clientKey)
        val paymentDataRepository = PaymentDataRepository(savedStateHandle)

        return AwaitComponent(
            action = action,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            redirectHandler = redirectHandler,
            statusRepository = statusRepository,
            paymentDataRepository = paymentDataRepository,
            componentParams = componentParams,
        )
    }
}
