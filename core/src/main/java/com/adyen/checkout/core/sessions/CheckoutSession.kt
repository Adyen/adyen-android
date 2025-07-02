/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.sessions

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.data.model.PaymentMethod
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupResponse

/**
 * A class holding the data required to launch Drop-in or a component with the sessions flow.
 * Use [CheckoutSessionProvider.createSession] to create this class.
 */
data class CheckoutSession(
    val sessionSetupResponse: SessionSetupResponse,
    // TODO - Partial Payment Flow Support
//    val order: Order?,
    val environment: Environment,
    val clientKey: String,
) {
    fun getPaymentMethod(paymentMethodType: String): PaymentMethod? {
        return sessionSetupResponse.paymentMethodsApiResponse?.paymentMethods.orEmpty()
            .firstOrNull {
                it.type == paymentMethodType
            }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun getConfiguration(): CheckoutConfiguration {
        return CheckoutConfiguration(
            environment = environment,
            clientKey = clientKey,
        )
    }
}
