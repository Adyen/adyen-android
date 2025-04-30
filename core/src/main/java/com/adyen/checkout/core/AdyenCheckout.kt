/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core

import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.CheckoutSessionProvider
import com.adyen.checkout.core.sessions.CheckoutSessionResult
import com.adyen.checkout.core.sessions.SessionModel

data class AdyenCheckout(
    val checkoutSession: CheckoutSession?,
) {

    companion object {
        suspend fun initialize(
            sessionModel: SessionModel,
            // TODO - Configuration
            clientKey: String,
        ): AdyenCheckout {
            return AdyenCheckout(
                checkoutSession = getCheckoutSession(sessionModel, clientKey),
            )
        }

        private suspend fun getCheckoutSession(
            sessionModel: SessionModel,
            clientKey: String,
            // TODO - Configuration
//        checkoutConfiguration: CheckoutConfiguration
        ): CheckoutSession? {
            return when (
                val result = CheckoutSessionProvider.createSession(sessionModel, Environment.TEST, clientKey)
            ) {
                is CheckoutSessionResult.Success -> result.checkoutSession
                is CheckoutSessionResult.Error -> {
                    null
                }
            }
        }
    }
}
