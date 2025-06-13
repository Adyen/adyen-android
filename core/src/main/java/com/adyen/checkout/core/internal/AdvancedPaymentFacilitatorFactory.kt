/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/5/2025.
 */

package com.adyen.checkout.core.internal

import com.adyen.checkout.core.CheckoutCallback
import com.adyen.checkout.core.CheckoutConfiguration
import com.adyen.checkout.core.internal.ui.model.ButtonComponentParamsMapper
import com.adyen.checkout.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.mbway.internal.ui.getMBWayConfiguration
import com.adyen.checkout.core.paymentmethod.PaymentComponentState
import com.adyen.checkout.core.paymentmethod.PaymentMethodDetails
import kotlinx.coroutines.CoroutineScope
import java.util.Locale

internal class AdvancedPaymentFacilitatorFactory(
    private val checkoutConfiguration: CheckoutConfiguration,
    private val checkoutCallback: CheckoutCallback?,
) : PaymentFacilitatorFactory {

    override fun create(coroutineScope: CoroutineScope): PaymentFacilitator {
        if (checkoutCallback == null) {
            throw IllegalArgumentException(
                "Checkout callback is not set. " +
                    "While using Advanced flow you must pass CheckoutCallback while initializing AdyenCheckout."
            )
        }

        val componentParams =
            ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
                checkoutConfiguration = checkoutConfiguration,

                // TODO - Add locale support, For now it's hardcoded to US
//        deviceLocale = localeProvider.getLocale(application)
                deviceLocale = Locale.US,
                dropInOverrideParams = null,
                componentSessionParams = null,
                componentConfiguration = checkoutConfiguration.getMBWayConfiguration(),
            )

        val componentEventHandler =
            AdvancedComponentEventHandler<PaymentComponentState<out PaymentMethodDetails>>(
                checkoutCallback,
            )

        // TODO - Advanced Flow
        return PaymentFacilitator(
            coroutineScope = coroutineScope,
            componentParams = componentParams,
            componentEventHandler = componentEventHandler,
        )
    }
}
