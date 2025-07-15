/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/6/2025.
 */

package com.adyen.checkout.core.components.internal

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.components.AdyenCheckout
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.sessions.internal.SessionsPaymentFacilitatorFactory
import kotlinx.coroutines.CoroutineScope

internal class PaymentFacilitatorProvider {

    fun provide(
        txVariant: String,
        adyenCheckout: AdyenCheckout,
        coroutineScope: CoroutineScope,
        savedStateHandle: SavedStateHandle,
        checkoutController: CheckoutController,
    ): PaymentFacilitator {
        val paymentFacilitatorFactory = if (adyenCheckout.checkoutSession == null) {
            AdvancedPaymentFacilitatorFactory(
                checkoutConfiguration = adyenCheckout.checkoutConfiguration,
                checkoutCallback = adyenCheckout.checkoutCallback,
                savedStateHandle = savedStateHandle,
                checkoutController = checkoutController,
            )
        } else {
            SessionsPaymentFacilitatorFactory(
                checkoutSession = adyenCheckout.checkoutSession,
                checkoutConfiguration = adyenCheckout.checkoutConfiguration,
                checkoutCallback = adyenCheckout.checkoutCallback,
                savedStateHandle = savedStateHandle,
                checkoutController = checkoutController,
            )
        }

        return paymentFacilitatorFactory.create(txVariant = txVariant, coroutineScope = coroutineScope)
    }
}
