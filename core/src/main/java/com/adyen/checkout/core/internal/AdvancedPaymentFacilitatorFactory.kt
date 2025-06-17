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
    private val checkoutCallback: CheckoutCallback?,
) : PaymentFacilitatorFactory {

    override fun create(txVariant: String, coroutineScope: CoroutineScope): PaymentFacilitator {
        if (checkoutCallback == null) {
            throw IllegalArgumentException(
                "Checkout callback is not set. " +
                    "While using Advanced flow you must pass CheckoutCallback while initializing AdyenCheckout."
            )
        }

        val paymentDelegate = PaymentMethodProvider.get(txVariant, coroutineScope, checkoutConfiguration)

        val componentEventHandler =
            AdvancedComponentEventHandler<BaseComponentState>(
                checkoutCallback,
            )

        return PaymentFacilitator(
            paymentDelegate = paymentDelegate,
            coroutineScope = coroutineScope,
            componentEventHandler = componentEventHandler,
        )
    }
}
