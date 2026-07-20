/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.await.internal.ui

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.AwaitAction
import com.adyen.checkout.core.action.internal.ActionFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.components.internal.PaymentDataRepository
import com.adyen.checkout.core.components.internal.data.api.DefaultStatusRepository
import com.adyen.checkout.core.components.internal.data.api.StatusService
import com.adyen.checkout.core.redirect.internal.DefaultRedirectHandler
import kotlinx.coroutines.CoroutineScope

internal class AwaitFactory : ActionFactory<AwaitAction, AwaitComponent> {

    override fun create(
        action: AwaitAction,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        params: CheckoutParams,
        savedStateHandle: SavedStateHandle,
    ): AwaitComponent {
        val redirectHandler = DefaultRedirectHandler()
        val httpClient = HttpClientFactory.getHttpClient(params.environment)
        val statusService = StatusService(httpClient)
        val statusRepository = DefaultStatusRepository(statusService, params.clientKey)
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
