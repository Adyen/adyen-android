/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2025.
 */

package com.adyen.checkout.mbway.internal.ui

import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.PaymentMethodFactory
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import com.adyen.checkout.core.components.internal.ui.state.DefaultStateManager
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateFactory
import com.adyen.checkout.mbway.internal.ui.state.MBWayPaymentComponentState
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewStateFactory
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewStateValidator
import kotlinx.coroutines.CoroutineScope

internal class MBWayFactory : PaymentMethodFactory<MBWayPaymentComponentState, MBWayComponent> {

    override fun create(
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
    ): MBWayComponent {
        val stateManager = DefaultStateManager(
            viewStateFactory = MBWayViewStateFactory(componentParamsBundle.commonComponentParams),
            componentStateFactory = MBWayComponentStateFactory(),
        )

        return MBWayComponent(
            componentParams = componentParamsBundle.commonComponentParams,
            analyticsManager = analyticsManager,
            stateManager = stateManager,
            validator = MBWayViewStateValidator(),
            // TODO - Order to be passed later
            order = null,
        )
    }
}
