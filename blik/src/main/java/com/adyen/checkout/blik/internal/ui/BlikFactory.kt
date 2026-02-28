/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui

import android.app.Application
import com.adyen.checkout.blik.internal.ui.state.BlikComponentStateFactory
import com.adyen.checkout.blik.internal.ui.state.BlikComponentStateReducer
import com.adyen.checkout.blik.internal.ui.state.BlikComponentStateValidator
import com.adyen.checkout.blik.internal.ui.state.BlikPaymentComponentState
import com.adyen.checkout.blik.internal.ui.state.BlikViewStateProducer
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.PaymentMethod
import com.adyen.checkout.core.components.data.model.StoredPaymentMethod
import com.adyen.checkout.core.components.internal.PaymentComponentFactory
import com.adyen.checkout.core.components.internal.StoredPaymentComponentFactory
import com.adyen.checkout.core.components.internal.data.provider.DefaultSdkDataProvider
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import kotlinx.coroutines.CoroutineScope

internal class BlikFactory :
    PaymentComponentFactory<BlikPaymentComponentState, BlikComponent>,
    StoredPaymentComponentFactory<BlikPaymentComponentState, StoredBlikComponent> {

    @Suppress("UNUSED_PARAMETER")
    override fun create(
        application: Application,
        paymentMethod: PaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
        checkoutCallbacks: CheckoutCallbacks,
    ): BlikComponent {
        val componentParams = componentParamsBundle.commonComponentParams
        return BlikComponent(
            componentParams = componentParams,
            analyticsManager = analyticsManager,
            sdkDataProvider = DefaultSdkDataProvider(analyticsManager),
            componentStateFactory = BlikComponentStateFactory(),
            componentStateReducer = BlikComponentStateReducer(),
            componentStateValidator = BlikComponentStateValidator(),
            viewStateProducer = BlikViewStateProducer(),
            coroutineScope = coroutineScope,
        )
    }

    @Suppress("UNUSED_PARAMETER")
    override fun create(
        application: Application,
        storedPaymentMethod: StoredPaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
        checkoutCallbacks: CheckoutCallbacks,
    ): StoredBlikComponent {
        val componentParams = componentParamsBundle.commonComponentParams
        return StoredBlikComponent(
            storedPaymentMethod = storedPaymentMethod,
            analyticsManager = analyticsManager,
            componentParams = componentParams,
            sdkDataProvider = DefaultSdkDataProvider(analyticsManager),
            coroutineScope = coroutineScope,
        )
    }
}
