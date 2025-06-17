/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/11/2022.
 */

package com.adyen.checkout.sessions.core

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.core.old.Environment

/**
 * A class holding the data required to launch Drop-in or a component with the sessions flow.
 * Use [CheckoutSessionProvider.createSession] to create this class.
 */
data class CheckoutSession(
    val sessionSetupResponse: SessionSetupResponse,
    val order: Order?,
    val environment: Environment,
    val clientKey: String,
) {
    fun getPaymentMethod(paymentMethodType: String): PaymentMethod? {
        return sessionSetupResponse.paymentMethodsApiResponse?.paymentMethods.orEmpty().firstOrNull {
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
