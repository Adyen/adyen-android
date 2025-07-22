/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/5/2025.
 */

package com.adyen.checkout.core.components.internal

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.internal.ActionProvider
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutController
import kotlinx.coroutines.CoroutineScope

internal class AdvancedPaymentFacilitatorFactory(
    private val checkoutConfiguration: CheckoutConfiguration,
    private val checkoutCallbacks: CheckoutCallbacks?,
    private val savedStateHandle: SavedStateHandle,
    private val checkoutController: CheckoutController,
) : PaymentFacilitatorFactory {

    override fun create(txVariant: String, coroutineScope: CoroutineScope): PaymentFacilitator {
        if (checkoutCallbacks == null) {
            throw IllegalArgumentException(
                "Checkout callback is not set. " +
                    "While using Advanced flow you must pass CheckoutCallback while initializing AdyenCheckout.",
            )
        }

        val paymentComponent = PaymentMethodProvider.get(
            txVariant = txVariant,
            coroutineScope = coroutineScope,
            checkoutConfiguration = checkoutConfiguration,
            componentSessionParams = null,
        )

        val componentEventHandler =
            AdvancedComponentEventHandler<BasePaymentComponentState>(
                checkoutCallbacks,
            )

        val actionProvider = ActionProvider(
            checkoutConfiguration = checkoutConfiguration,
            savedStateHandle = savedStateHandle,
        )

        return PaymentFacilitator(
            paymentComponent = paymentComponent,
            coroutineScope = coroutineScope,
            componentEventHandler = componentEventHandler,
            actionProvider = actionProvider,
            checkoutController = checkoutController,
        )
    }
}
