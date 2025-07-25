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
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.components.internal.ui.state.DefaultComponentStateManager
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.core.sessions.internal.model.SessionParams
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateFactory
import com.adyen.checkout.mbway.internal.ui.state.MBWayPaymentComponentState
import com.adyen.checkout.mbway.internal.ui.state.MBWayStateUpdaterRegistry
import com.adyen.checkout.mbway.internal.ui.state.MBWayTransformerRegistry
import com.adyen.checkout.mbway.internal.ui.state.MBWayValidatorRegistry
import kotlinx.coroutines.CoroutineScope
import java.util.Locale

internal class MBWayFactory : PaymentMethodFactory<MBWayPaymentComponentState, MBWayComponent> {

    override fun create(
        coroutineScope: CoroutineScope,
        checkoutConfiguration: CheckoutConfiguration,
        componentSessionParams: SessionParams?,
    ): MBWayComponent {
        val componentParams = CommonComponentParamsMapper().mapToParams(
            checkoutConfiguration = checkoutConfiguration,

            // TODO - Add locale support, For now it's hardcoded to US
            // deviceLocale = localeProvider.getLocale(application)
            deviceLocale = Locale.US,
            dropInOverrideParams = null,
            componentSessionParams = componentSessionParams,
        ).commonComponentParams

        // TODO - Analytics to be passed later, given that Drop-in might pass its own AnalyticsManager?
        // TODO - Analytics. We might need to change the logic on AnalyticsManager creation.
        val analyticsManager = AnalyticsManagerFactory().provide(
            componentParams = componentParams,
            application = null,
            source = AnalyticsSource.PaymentComponent(PaymentMethodTypes.MB_WAY),
            // TODO - When we move out componentParams logic creation to the payment facilitator
            //  factory level, Analytics manager should move there too and sessionId can be passed
            sessionId = null,
        )

        val transformerRegistry = MBWayTransformerRegistry()
        val componentStateFactory = MBWayComponentStateFactory(componentParams)
        val stateManager = DefaultComponentStateManager(
            factory = componentStateFactory,
            validationRegistry = MBWayValidatorRegistry(),
            stateUpdaterRegistry = MBWayStateUpdaterRegistry(),
            transformerRegistry = transformerRegistry,
        )

        return MBWayComponent(
            coroutineScope = coroutineScope,
            componentParams = componentParams,
            analyticsManager = analyticsManager,
            // TODO - Order to be passed later
            order = null,
            transformerRegistry = transformerRegistry,
            stateManager = stateManager,
        )
    }
}
