/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 26/8/2026.
 */

package com.adyen.checkout.core.common

import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods

/**
 * Builds real [CheckoutContext] instances for tests.
 *
 * The constructors of the [CheckoutContext] subtypes are internal to this module, since an instance is only ever
 * meant to come out of `Checkout.setup`. Tests in other modules therefore cannot build one, and cannot fake one
 * either: [CheckoutContext] is a sealed interface and its subtypes are final data classes. This fixture hands out
 * the real thing instead, so that no test has to mock a sealed subtype.
 */
object TestCheckoutContext {

    const val TEST_CHECKOUT_ATTEMPT_ID = "test_checkout_attempt_id"

    fun advanced(
        paymentMethods: PaymentMethods,
        checkoutConfiguration: CheckoutConfiguration,
        checkoutAttemptId: String = TEST_CHECKOUT_ATTEMPT_ID,
        publicKey: String? = null,
    ): CheckoutContext.Advanced = CheckoutContext.Advanced(
        paymentMethods = paymentMethods,
        checkoutConfiguration = checkoutConfiguration,
        checkoutAttemptId = checkoutAttemptId,
        publicKey = publicKey,
    )
}
