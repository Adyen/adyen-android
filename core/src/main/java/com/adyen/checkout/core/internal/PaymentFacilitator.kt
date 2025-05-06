/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.AdyenCheckout
import com.adyen.checkout.core.internal.ui.PaymentDelegate
import com.adyen.checkout.core.internal.ui.model.ButtonComponentParamsMapper
import com.adyen.checkout.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.internal.ui.model.SessionParamsFactory
import com.adyen.checkout.core.mbway.internal.ui.MBWayDelegate
import com.adyen.checkout.core.mbway.internal.ui.getMBWayConfiguration
import kotlinx.coroutines.CoroutineScope
import java.util.Locale

internal class PaymentFacilitator(coroutineScope: CoroutineScope, adyenCheckout: AdyenCheckout) {

    private val componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
        checkoutConfiguration = adyenCheckout.checkoutConfiguration,

        // TODO - Add locale support, For now it's hardcoded to US
//        deviceLocale = localeProvider.getLocale(application)
        deviceLocale = Locale.US,
        dropInOverrideParams = null,
        componentSessionParams = adyenCheckout.checkoutSession?.let {
            SessionParamsFactory.create(adyenCheckout.checkoutSession)
        },
        componentConfiguration = adyenCheckout.checkoutConfiguration.getMBWayConfiguration(),
    )

    // TODO - Make it a val, initialize it based on txVariant?
    private val paymentDelegate: PaymentDelegate = MBWayDelegate(coroutineScope, componentParams)

    @Composable
    fun ViewFactory(modifier: Modifier = Modifier) {
        paymentDelegate.ViewFactory(modifier)
    }

    fun submit() {
        paymentDelegate.submit()
    }
}
