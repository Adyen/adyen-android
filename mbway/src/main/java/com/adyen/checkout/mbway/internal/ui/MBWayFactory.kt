/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2025.
 */

package com.adyen.checkout.mbway.internal.ui

import com.adyen.checkout.core.analytics.internal.AnalyticsManagerFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsSource
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.PaymentMethodFactory
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParamsMapperData
import com.adyen.checkout.core.components.internal.ui.state.DefaultViewStateManager
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.mbway.internal.ui.state.MBWayPaymentComponentState
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewStateFactory
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewStateValidator
import kotlinx.coroutines.CoroutineScope

internal class MBWayFactory : PaymentMethodFactory<MBWayPaymentComponentState, MBWayComponent> {

    override fun create(
        coroutineScope: CoroutineScope,
        checkoutConfiguration: CheckoutConfiguration,
        commonComponentParamsMapperData: CommonComponentParamsMapperData,
    ): MBWayComponent {
        // TODO - Analytics to be passed later, given that Drop-in might pass its own AnalyticsManager?
        // TODO - Analytics. We might need to change the logic on AnalyticsManager creation.
        val analyticsManager = AnalyticsManagerFactory().provide(
            componentParams = commonComponentParamsMapperData.commonComponentParams,
            application = null,
            source = AnalyticsSource.PaymentComponent(PaymentMethodTypes.MB_WAY),
            // TODO - When we move out componentParams logic creation to the payment facilitator
            //  factory level, Analytics manager should move there too and sessionId can be passed
            sessionId = null,
        )

        val stateManager = DefaultViewStateManager(
            factory = MBWayViewStateFactory(commonComponentParamsMapperData.commonComponentParams),
            validator = MBWayViewStateValidator(),
        )

        return MBWayComponent(
            componentParams = commonComponentParamsMapperData.commonComponentParams,
            analyticsManager = analyticsManager,
            viewStateManager = stateManager,
            // TODO - Order to be passed later
            order = null,
        )
    }
}
