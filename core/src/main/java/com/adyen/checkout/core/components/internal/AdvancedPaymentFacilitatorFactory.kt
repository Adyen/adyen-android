/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/5/2025.
 */

package com.adyen.checkout.core.components.internal

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.internal.ActionProvider
import com.adyen.checkout.core.analytics.internal.AnalyticsManagerFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsSource
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParamsMapper
import kotlinx.coroutines.CoroutineScope
import java.util.Locale

internal class AdvancedPaymentFacilitatorFactory(
    private val checkoutConfiguration: CheckoutConfiguration,
    private val checkoutCallbacks: CheckoutCallbacks,
    private val savedStateHandle: SavedStateHandle,
    private val checkoutController: CheckoutController,
) : PaymentFacilitatorFactory {

    override fun create(txVariant: String, coroutineScope: CoroutineScope): PaymentFacilitator {
        val componentParamsBundle = CommonComponentParamsMapper().mapToParams(
            checkoutConfiguration = checkoutConfiguration,

            // TODO - Add locale support, For now it's hardcoded to US
            // deviceLocale = localeProvider.getLocale(application)
            deviceLocale = Locale.US,
            dropInOverrideParams = null,
            componentSessionParams = null,
        )

        // TODO - Analytics. We might need to change the logic on AnalyticsManager creation.
        val analyticsManager = AnalyticsManagerFactory().provide(
            componentParams = componentParamsBundle.commonComponentParams,
            application = null,
            // TODO - Analytics. Provide payment method type to source
            source = AnalyticsSource.PaymentComponent("AwaitAction"),
            sessionId = null,
        )

        val paymentComponent = PaymentMethodProvider.get(
            txVariant = txVariant,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            checkoutConfiguration = checkoutConfiguration,
            componentParamsBundle = componentParamsBundle,
        )

        val componentEventHandler = AdvancedComponentEventHandler<BasePaymentComponentState>(
            componentCallbacks = checkoutCallbacks.toAdvancedComponentCallbacks(),
        )

        val actionProvider = ActionProvider(
            analyticsManager = analyticsManager,
            checkoutConfiguration = checkoutConfiguration,
            savedStateHandle = savedStateHandle,
            commonComponentParams = componentParamsBundle.commonComponentParams,
        )

        return PaymentFacilitator(
            paymentComponent = paymentComponent,
            coroutineScope = coroutineScope,
            componentEventHandler = componentEventHandler,
            actionProvider = actionProvider,
            checkoutController = checkoutController,
            shopperLocale = componentParamsBundle.commonComponentParams.shopperLocale,
        )
    }
}
