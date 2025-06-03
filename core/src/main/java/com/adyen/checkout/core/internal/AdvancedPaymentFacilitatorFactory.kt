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
import kotlinx.coroutines.CoroutineScope

internal class AdvancedPaymentFacilitatorFactory(
    private val checkoutConfiguration: CheckoutConfiguration,
    private val checkoutCallback: CheckoutCallback,
) : PaymentFacilitatorFactory {

    override fun create(coroutineScope: CoroutineScope): PaymentFacilitator {
        // TODO - Advanced Flow
        return PaymentFacilitator(
            coroutineScope = coroutineScope,
            checkoutSession = null,
            checkoutConfiguration = checkoutConfiguration,
            checkoutCallback = checkoutCallback,
            sessionInteractor = null,
        )
    }
}
