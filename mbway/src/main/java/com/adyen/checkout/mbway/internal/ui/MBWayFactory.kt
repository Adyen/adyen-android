/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2025.
 */

package com.adyen.checkout.mbway.internal.ui

import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.CheckoutAdditionalCallback
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.internal.PaymentComponentFactory
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateFactory
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateReducer
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateValidator
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewStateProducer
import kotlinx.coroutines.CoroutineScope

internal class MBWayFactory : PaymentComponentFactory<MBWayComponent> {

    override fun create(
        paymentMethod: PaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        sdkDataProvider: SdkDataProvider,
        params: CheckoutParams,
        additionalCallbacks: Set<CheckoutAdditionalCallback>,
    ): MBWayComponent {
        return MBWayComponent(
            analyticsManager = analyticsManager,
            sdkDataProvider = sdkDataProvider,
            componentStateFactory = MBWayComponentStateFactory(params.shopperLocale),
            componentStateReducer = MBWayComponentStateReducer(),
            componentStateValidator = MBWayComponentStateValidator(),
            viewStateProducer = MBWayViewStateProducer(),
            coroutineScope = coroutineScope,
        )
    }
}
