/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/6/2025.
 */

package com.adyen.checkout.core.internal

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.components.AdyenCheckout
import kotlinx.coroutines.CoroutineScope

internal class PaymentFacilitatorProvider {

    fun provide(
        txVariant: String,
        adyenCheckout: AdyenCheckout,
        coroutineScope: CoroutineScope,
        savedStateHandle: SavedStateHandle,
    ): PaymentFacilitator {
        val paymentFacilitatorFactory = if (adyenCheckout.checkoutSession == null) {
            AdvancedPaymentFacilitatorFactory(
                checkoutConfiguration = adyenCheckout.checkoutConfiguration,
                checkoutCallback = adyenCheckout.checkoutCallback,
            )
        } else {
            SessionsPaymentFacilitatorFactory(
                checkoutSession = adyenCheckout.checkoutSession,
                checkoutConfiguration = adyenCheckout.checkoutConfiguration,
                checkoutCallback = adyenCheckout.checkoutCallback,
                savedStateHandle = savedStateHandle,
            )
        }

        return paymentFacilitatorFactory.create(txVariant = txVariant, coroutineScope = coroutineScope)
    }
}
