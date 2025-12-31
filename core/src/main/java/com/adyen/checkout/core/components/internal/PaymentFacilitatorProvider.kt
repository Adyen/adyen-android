/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/6/2025.
 */

package com.adyen.checkout.core.components.internal

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.components.AdyenPaymentFlowKey
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.sessions.internal.SessionsPaymentFacilitatorFactory
import kotlinx.coroutines.CoroutineScope

internal class PaymentFacilitatorProvider {

    @Suppress("LongParameterList")
    fun provide(
        key: AdyenPaymentFlowKey,
        checkoutContext: CheckoutContext,
        checkoutCallbacks: CheckoutCallbacks,
        checkoutController: CheckoutController,
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
                    checkoutAttemptId = checkoutContext.checkoutAttemptId,
                    paymentMethodsApiResponse = checkoutContext.paymentMethodsApiResponse,
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
                    checkoutAttemptId = checkoutContext.checkoutAttemptId,
                    paymentMethodsApiResponse =
                        checkoutContext.checkoutSession.sessionSetupResponse.paymentMethodsApiResponse
                            ?: error("PaymentMethodsApiResponse cannot be null."),
                )
            }
        }
        return paymentFacilitatorFactory.create(key, coroutineScope)
    }
}
