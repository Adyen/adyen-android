/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.components.internal.CheckoutInitializer
import com.adyen.checkout.core.sessions.SessionModel

object Checkout {

    suspend fun initialize(
        sessionModel: SessionModel,
        checkoutConfiguration: CheckoutConfiguration,
        checkoutCallbacks: CheckoutCallbacks,
    ): Result {
        val initializationData = CheckoutInitializer.initialize(
            checkoutConfiguration = checkoutConfiguration,
            sessionModel = sessionModel,
        )

        return when {
            initializationData.checkoutSession == null -> {
                Result.Error("Failed to initialize sessions.")
            }

            else -> Result.Success(
                checkoutContext = CheckoutContext.Sessions(
                    checkoutSession = initializationData.checkoutSession,
                    checkoutConfiguration = checkoutConfiguration,
                    checkoutCallbacks = checkoutCallbacks,
                    publicKey = initializationData.publicKey,
                ),
            )
        }
    }

    suspend fun initialize(
        paymentMethodsApiResponse: PaymentMethodsApiResponse,
        checkoutConfiguration: CheckoutConfiguration,
        checkoutCallbacks: CheckoutCallbacks,
    ): Result {
        val initializationData = CheckoutInitializer.initialize(
            checkoutConfiguration = checkoutConfiguration,
            sessionModel = null,
        )

        return Result.Success(
            CheckoutContext.Advanced(
                paymentMethodsApiResponse = paymentMethodsApiResponse,
                checkoutConfiguration = checkoutConfiguration,
                checkoutCallbacks = checkoutCallbacks,
                publicKey = initializationData.publicKey,
            ),
        )
    }

    sealed interface Result {
        data class Success(val checkoutContext: CheckoutContext) : Result
        data class Error(val errorReason: String) : Result
    }
}
