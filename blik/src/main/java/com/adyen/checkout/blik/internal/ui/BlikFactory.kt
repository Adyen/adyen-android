/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui

import com.adyen.checkout.blik.internal.ui.state.BlikComponentStateFactory
import com.adyen.checkout.blik.internal.ui.state.BlikComponentStateReducer
import com.adyen.checkout.blik.internal.ui.state.BlikComponentStateValidator
import com.adyen.checkout.blik.internal.ui.state.BlikPaymentComponentState
import com.adyen.checkout.blik.internal.ui.state.BlikViewStateProducer
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.components.CheckoutAdditionalCallback
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.internal.PaymentComponentFactory
import com.adyen.checkout.core.components.internal.StoredPaymentComponentFactory
import com.adyen.checkout.core.components.internal.data.provider.DefaultSdkDataProvider
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import kotlinx.coroutines.CoroutineScope

internal class BlikFactory :
    PaymentComponentFactory<BlikPaymentComponentState, BlikComponent>,
    StoredPaymentComponentFactory<BlikPaymentComponentState, StoredBlikComponent> {

    override fun create(
        paymentMethod: PaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
        additionalCallbacks: Set<CheckoutAdditionalCallback>,
    ): BlikComponent {
        return BlikComponent(
            analyticsManager = analyticsManager,
            sdkDataProvider = DefaultSdkDataProvider(analyticsManager),
            componentStateFactory = BlikComponentStateFactory(),
            componentStateReducer = BlikComponentStateReducer(),
            componentStateValidator = BlikComponentStateValidator(),
            viewStateProducer = BlikViewStateProducer(),
            coroutineScope = coroutineScope,
        )
    }

    override fun create(
        storedPaymentMethod: StoredPaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
    ): StoredBlikComponent {
        return StoredBlikComponent(
            storedPaymentMethod = storedPaymentMethod,
            analyticsManager = analyticsManager,
            sdkDataProvider = DefaultSdkDataProvider(analyticsManager),
            coroutineScope = coroutineScope,
        )
    }
}
