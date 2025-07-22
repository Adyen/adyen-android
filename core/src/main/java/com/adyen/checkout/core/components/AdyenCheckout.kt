/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.CheckoutSessionResult
import com.adyen.checkout.core.sessions.SessionModel
import com.adyen.checkout.core.sessions.internal.CheckoutSessionProvider

object AdyenCheckout {

    suspend fun initialize(
        sessionModel: SessionModel,
        checkoutConfiguration: CheckoutConfiguration,
        checkoutCallbacks: CheckoutCallbacks?
    ): Result {
        val checkoutSession = getCheckoutSession(sessionModel, checkoutConfiguration)
        return when {
            checkoutSession != null -> Result.Success(
                checkoutContext = CheckoutContext.Sessions(
                    checkoutSession = checkoutSession,
                    checkoutConfiguration = checkoutConfiguration,
                    checkoutCallbacks = checkoutCallbacks,
                ),
            )

            else -> Result.Error("Session initialization failed.")
        }
    }

    private suspend fun getCheckoutSession(
        sessionModel: SessionModel,
        checkoutConfiguration: CheckoutConfiguration,
    ): CheckoutSession? {
        return when (
            val result = CheckoutSessionProvider.createSession(sessionModel, checkoutConfiguration)
        ) {
            is CheckoutSessionResult.Success -> result.checkoutSession
            is CheckoutSessionResult.Error -> null
        }
    }

    sealed interface Result {
        data class Success(val checkoutContext: CheckoutContext) : Result
        data class Error(val errorReason: String) : Result
    }
}
