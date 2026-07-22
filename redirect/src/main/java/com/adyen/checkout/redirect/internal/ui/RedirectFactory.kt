/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 29/10/2025.
 */

package com.adyen.checkout.redirect.internal.ui

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.RedirectAction
import com.adyen.checkout.core.action.internal.ActionFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.components.internal.PaymentDataRepository
import com.adyen.checkout.core.redirect.internal.DefaultRedirectHandler
import com.adyen.checkout.redirect.internal.data.api.NativeRedirectService
import kotlinx.coroutines.CoroutineScope

internal class RedirectFactory : ActionFactory<RedirectAction, RedirectComponent> {

    override fun create(
        action: RedirectAction,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        params: CheckoutParams,
        savedStateHandle: SavedStateHandle,
    ): RedirectComponent {
        val redirectHandler = DefaultRedirectHandler()
        val paymentDataRepository = PaymentDataRepository(savedStateHandle)
        val httpClient = HttpClientFactory.getHttpClient(params.environment)
        val nativeRedirectService = NativeRedirectService(httpClient)

        return RedirectComponent(
            action = action,
            analyticsManager = analyticsManager,
            redirectHandler = redirectHandler,
            paymentDataRepository = paymentDataRepository,
            nativeRedirectService = nativeRedirectService,
            clientKey = params.clientKey,
            coroutineScope = coroutineScope,
        )
    }
}
