/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/12/2025.
 */

package com.adyen.checkout.core.components.internal.helper

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.internal.AdvancedPaymentFacilitatorFactory
import com.adyen.checkout.core.components.internal.PaymentFacilitator
import com.adyen.checkout.core.components.internal.PaymentFacilitatorFactory
import com.adyen.checkout.core.components.internal.PaymentFacilitatorProvider
import com.adyen.checkout.core.sessions.internal.SessionsPaymentFacilitatorFactory

@Suppress("LongParameterList")
internal fun PaymentFacilitatorProvider.createPaymentFacilitator(
    applicationContext: Context,
    savedStateHandle: SavedStateHandle,
    checkoutContext: CheckoutContext,
    checkoutCallbacks: CheckoutCallbacks,
    checkoutController: CheckoutController,
    factoryMethod: (PaymentFacilitatorFactory) -> PaymentFacilitator,
): PaymentFacilitator {
    val paymentFacilitatorFactory = when (checkoutContext) {
        is CheckoutContext.Advanced -> {
            AdvancedPaymentFacilitatorFactory(
                applicationContext = applicationContext,
                checkoutConfiguration = checkoutContext.checkoutConfiguration,
                checkoutCallbacks = checkoutCallbacks,
                savedStateHandle = savedStateHandle,
                checkoutController = checkoutController,
                publicKey = checkoutContext.publicKey,
            )
        }

        is CheckoutContext.Sessions -> {
            SessionsPaymentFacilitatorFactory(
                applicationContext = applicationContext,
                checkoutSession = checkoutContext.checkoutSession,
                checkoutConfiguration = checkoutContext.checkoutConfiguration,
                checkoutCallbacks = checkoutCallbacks,
                savedStateHandle = savedStateHandle,
                checkoutController = checkoutController,
                publicKey = checkoutContext.publicKey,
            )
        }
    }

    return factoryMethod(paymentFacilitatorFactory)
}
