/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 29/10/2025.
 */

package com.adyen.checkout.redirect.internal.ui

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.data.RedirectAction
import com.adyen.checkout.core.action.internal.ActionFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.api.HttpClientFactory
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.PaymentDataRepository
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams
import com.adyen.checkout.core.redirect.internal.DefaultRedirectHandler
import com.adyen.checkout.redirect.internal.data.api.NativeRedirectService
import kotlinx.coroutines.CoroutineScope

internal class RedirectFactory : ActionFactory<RedirectComponent> {

    @Suppress("TooGenericExceptionThrown")
    override fun create(
        action: Action,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        savedStateHandle: SavedStateHandle,
        commonComponentParams: CommonComponentParams
    ): RedirectComponent {
        if (action !is RedirectAction) {
//          TODO - Error Propagation
//          throw ComponentException("Unsupported action")
            throw RuntimeException("Unsupported action")
        }

        val redirectHandler = DefaultRedirectHandler()
        val paymentDataRepository = PaymentDataRepository(savedStateHandle)
        val httpClient = HttpClientFactory.getHttpClient(commonComponentParams.environment)
        val nativeRedirectService = NativeRedirectService(httpClient)

        return RedirectComponent(
            action = action,
            analyticsManager = analyticsManager,
            redirectHandler = redirectHandler,
            paymentDataRepository = paymentDataRepository,
            nativeRedirectService = nativeRedirectService,
            componentParams = commonComponentParams,
        )
    }
}
