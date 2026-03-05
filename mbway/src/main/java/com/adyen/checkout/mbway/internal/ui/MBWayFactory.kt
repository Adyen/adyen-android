/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2025.
 */

package com.adyen.checkout.mbway.internal.ui

import android.app.Application
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.PaymentMethod
import com.adyen.checkout.core.components.internal.PaymentComponentFactory
import com.adyen.checkout.core.components.internal.data.provider.DefaultSdkDataProvider
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateFactory
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateReducer
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateValidator
import com.adyen.checkout.mbway.internal.ui.state.MBWayPaymentComponentState
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewStateProducer
import kotlinx.coroutines.CoroutineScope

internal class MBWayFactory : PaymentComponentFactory<MBWayPaymentComponentState, MBWayComponent> {

    @Suppress("UNUSED_PARAMETER")
    override fun create(
        application: Application,
        paymentMethod: PaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
        checkoutCallbacks: CheckoutCallbacks,
    ): MBWayComponent {
        val componentParams = componentParamsBundle.commonComponentParams
        return MBWayComponent(
            componentParams = componentParams,
            analyticsManager = analyticsManager,
            sdkDataProvider = DefaultSdkDataProvider(analyticsManager),
            componentStateFactory = MBWayComponentStateFactory(componentParams),
            componentStateReducer = MBWayComponentStateReducer(),
            componentStateValidator = MBWayComponentStateValidator(),
            viewStateProducer = MBWayViewStateProducer(),
            coroutineScope = coroutineScope,
        )
    }
}
