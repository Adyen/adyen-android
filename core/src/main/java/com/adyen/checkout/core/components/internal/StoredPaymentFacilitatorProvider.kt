/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/12/2025.
 */

package com.adyen.checkout.core.components.internal

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.data.model.StoredPaymentMethod
import com.adyen.checkout.core.sessions.internal.SessionsPaymentFacilitatorFactory
import kotlinx.coroutines.CoroutineScope

internal class StoredPaymentFacilitatorProvider(
    private val storedPaymentMethod: StoredPaymentMethod,
    private val checkoutContext: CheckoutContext,
    private val checkoutCallbacks: CheckoutCallbacks,
    private val checkoutController: CheckoutController,
) : PaymentFacilitatorProvider {

    override fun provide(
        applicationContext: Context,
        coroutineScope: CoroutineScope,
        savedStateHandle: SavedStateHandle,
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

        return paymentFacilitatorFactory.create(
            storedPaymentMethod = storedPaymentMethod,
            coroutineScope = coroutineScope,
        )
    }
}
