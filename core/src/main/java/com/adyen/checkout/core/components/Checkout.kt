/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.components.internal.CheckoutInitializer
import com.adyen.checkout.core.components.internal.validate
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.sessions.SessionResponse

object Checkout {

    suspend fun setup(
        sessionResponse: SessionResponse,
        configuration: CheckoutConfiguration,
    ): Result<CheckoutContext.Sessions> {
        configuration.validate()?.let { error ->
            return Result.Error(error)
        }

        val initializationData = CheckoutInitializer.initialize(
            checkoutConfiguration = configuration,
            sessionResponse = sessionResponse,
        )

        return when {
            initializationData.checkoutSession == null -> Result.Error(
                CheckoutError(
                    code = CheckoutError.ErrorCode.SESSION_SETUP_FAILURE,
                    message = "Failed to initialize sessions.",
                ),
            )

            else -> Result.Success(
                checkoutContext = CheckoutContext.Sessions(
                    checkoutSession = initializationData.checkoutSession,
                    checkoutConfiguration = configuration,
                    checkoutAttemptId = initializationData.checkoutAttemptId,
                    publicKey = initializationData.publicKey,
                ),
            )
        }
    }

    suspend fun setup(
        paymentMethodsApiResponse: PaymentMethodsApiResponse,
        configuration: CheckoutConfiguration,
    ): Result<CheckoutContext.Advanced> {
        configuration.validate()?.let { error ->
            return Result.Error(error)
        }

        val initializationData = CheckoutInitializer.initialize(
            checkoutConfiguration = configuration,
            sessionResponse = null,
        )

        return Result.Success(
            CheckoutContext.Advanced(
                paymentMethodsApiResponse = paymentMethodsApiResponse,
                checkoutConfiguration = configuration,
                checkoutAttemptId = initializationData.checkoutAttemptId,
                publicKey = initializationData.publicKey,
            ),
        )
    }

    sealed interface Result<T : CheckoutContext> {
        data class Success<T : CheckoutContext>(val checkoutContext: T) : Result<T>
        data class Error<T : CheckoutContext>(val error: CheckoutError) : Result<T>
    }
}
