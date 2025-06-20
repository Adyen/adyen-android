/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/6/2025.
 */

package com.adyen.checkout.core.mbway.internal.ui

import com.adyen.checkout.core.CheckoutConfiguration
import com.adyen.checkout.core.internal.PaymentMethodFactory
import com.adyen.checkout.core.internal.ui.model.ButtonComponentParamsMapper
import com.adyen.checkout.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.internal.ui.model.SessionParamsFactory
import com.adyen.checkout.core.sessions.CheckoutSession
import kotlinx.coroutines.CoroutineScope
import java.util.Locale

// TODO - Add Initializer
internal class MBWayFactory : PaymentMethodFactory<MBWayComponentState, MBWayDelegate> {

    override fun create(
        coroutineScope: CoroutineScope,
        checkoutConfiguration: CheckoutConfiguration
    ): MBWayDelegate {
        val componentParams =
            ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,

                // TODO - Add locale support, For now it's hardcoded to US
                // deviceLocale = localeProvider.getLocale(application)
                deviceLocale = Locale.US,
                dropInOverrideParams = null,
                componentSessionParams = null,
                componentConfiguration = checkoutConfiguration.getMBWayConfiguration(),
            )

        return MBWayDelegate(coroutineScope, componentParams)
    }

    override fun create(
        coroutineScope: CoroutineScope,
        checkoutSession: CheckoutSession,
        checkoutConfiguration: CheckoutConfiguration
    ): MBWayDelegate {
        val componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
            checkoutConfiguration = checkoutConfiguration,

            // TODO - Add locale support, For now it's hardcoded to US
            // deviceLocale = localeProvider.getLocale(application)
            deviceLocale = Locale.US,
            dropInOverrideParams = null,
            componentSessionParams = SessionParamsFactory.create(checkoutSession),
            componentConfiguration = checkoutConfiguration.getMBWayConfiguration(),
        )

        return MBWayDelegate(coroutineScope, componentParams)
    }
}
