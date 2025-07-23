/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/6/2025.
 */

package com.adyen.checkout.core.components.internal

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.components.CheckoutContext
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.sessions.internal.SessionsPaymentFacilitatorFactory
import kotlinx.coroutines.CoroutineScope

internal class PaymentFacilitatorProvider {

    fun provide(
        txVariant: String,
        checkoutContext: CheckoutContext,
        coroutineScope: CoroutineScope,
        savedStateHandle: SavedStateHandle,
        checkoutController: CheckoutController,
    ): PaymentFacilitator {
        val paymentFacilitatorFactory = when (checkoutContext) {
            is CheckoutContext.Advanced -> {
                AdvancedPaymentFacilitatorFactory(
                    checkoutConfiguration = checkoutContext.checkoutConfiguration,
                    checkoutCallbacks = checkoutContext.checkoutCallbacks,
                    savedStateHandle = savedStateHandle,
                    checkoutController = checkoutController,
                )
            }

            is CheckoutContext.Sessions -> {
                SessionsPaymentFacilitatorFactory(
                    checkoutSession = checkoutContext.checkoutSession,
                    checkoutConfiguration = checkoutContext.checkoutConfiguration,
                    checkoutCallbacks = checkoutContext.checkoutCallbacks,
                    savedStateHandle = savedStateHandle,
                    checkoutController = checkoutController,
                )
            }
        }

        return paymentFacilitatorFactory.create(txVariant = txVariant, coroutineScope = coroutineScope)
    }
}
