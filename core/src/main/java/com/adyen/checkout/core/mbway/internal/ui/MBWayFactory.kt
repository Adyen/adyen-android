/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/6/2025.
 */

package com.adyen.checkout.core.mbway.internal.ui

import com.adyen.checkout.core.CheckoutConfiguration
import com.adyen.checkout.core.PaymentMethodTypes
import com.adyen.checkout.core.internal.PaymentMethodFactory
import com.adyen.checkout.core.internal.analytics.AnalyticsManagerFactory
import com.adyen.checkout.core.internal.analytics.AnalyticsSource
import com.adyen.checkout.core.internal.ui.model.ButtonComponentParamsMapper
import com.adyen.checkout.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.internal.ui.state.DefaultDelegateStateManager
import com.adyen.checkout.core.mbway.internal.ui.model.MBWayStateUpdaterRegistry
import com.adyen.checkout.core.mbway.internal.ui.model.MBWayTransformerRegistry
import com.adyen.checkout.core.mbway.internal.ui.model.MBWayValidatorRegistry
import kotlinx.coroutines.CoroutineScope
import java.util.Locale

// TODO - Add Initializer
internal class MBWayFactory : PaymentMethodFactory<MBWayComponentState, MBWayDelegate> {

    override fun create(
        coroutineScope: CoroutineScope,
        checkoutConfiguration: CheckoutConfiguration,
        componentSessionParams: SessionParams?,
    ): MBWayDelegate {
        val componentParams =
            ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,

                // TODO - Add locale support, For now it's hardcoded to US
                // deviceLocale = localeProvider.getLocale(application)
                deviceLocale = Locale.US,
                dropInOverrideParams = null,
                componentSessionParams = componentSessionParams,
                componentConfiguration = checkoutConfiguration.getMBWayConfiguration(),
            )

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
        val delegateStateFactory = MBWayDelegateStateFactory()
        val stateManager = DefaultDelegateStateManager(
            factory = delegateStateFactory,
            validationRegistry = MBWayValidatorRegistry(),
            stateUpdaterRegistry = MBWayStateUpdaterRegistry(),
            transformerRegistry = transformerRegistry,
        )

        return MBWayDelegate(
            coroutineScope = coroutineScope,
            componentParams = componentParams,
            analyticsManager = analyticsManager,
            order = null,
            transformerRegistry = transformerRegistry,
            stateManager = stateManager,
        )
    }
}
