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
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.PaymentDataRepository
import com.adyen.checkout.core.components.internal.data.api.DefaultStatusRepository
import com.adyen.checkout.core.components.internal.data.api.StatusService
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams
import com.adyen.checkout.core.redirect.internal.DefaultRedirectHandler
import kotlinx.coroutines.CoroutineScope

internal class AwaitFactory : ActionFactory<AwaitComponent> {

    @Suppress("TooGenericExceptionThrown")
    override fun create(
        action: Action,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        savedStateHandle: SavedStateHandle,
        commonComponentParams: CommonComponentParams,
    ): AwaitComponent {
        if (action !is AwaitAction) {
//          TODO - Error Propagation
//          throw ComponentException("Unsupported action")
            throw RuntimeException("Unsupported action")
        }

        val redirectHandler = DefaultRedirectHandler()
        val httpClient = HttpClientFactory.getHttpClient(commonComponentParams.environment)
        val statusService = StatusService(httpClient)
        val statusRepository = DefaultStatusRepository(statusService, commonComponentParams.clientKey)
        val paymentDataRepository = PaymentDataRepository(savedStateHandle)

        return AwaitComponent(
            action = action,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            redirectHandler = redirectHandler,
            statusRepository = statusRepository,
            paymentDataRepository = paymentDataRepository,
        )
    }
}
