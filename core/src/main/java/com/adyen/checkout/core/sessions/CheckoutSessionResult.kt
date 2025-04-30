/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.sessions

/**
 * The result of the API call to fetch a [CheckoutSession].
 */
sealed class CheckoutSessionResult {
    class Success(val checkoutSession: CheckoutSession) : CheckoutSessionResult()

    // TODO - Revisit while error propagation improvements
//    class Error(val exception: CheckoutException) : CheckoutSessionResult()
    class Error(val exception: Exception) : CheckoutSessionResult()
}
