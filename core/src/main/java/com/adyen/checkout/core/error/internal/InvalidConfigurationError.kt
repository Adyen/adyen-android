/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 3/8/2026.
 */

package com.adyen.checkout.core.error.internal

import androidx.annotation.RestrictTo

/**
 * Indicates that the configuration required to complete the checkout is missing or invalid.
 *
 * Use this for merchant facing configuration problems that can only be detected once the payment
 * method data is known, such as a missing merchant account. Configuration that can be validated up
 * front is instead reported through `CheckoutConfiguration.validate()`.
 *
 * @param message A human-readable description of the error.
 * @param cause The underlying cause of this error, if any.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class InvalidConfigurationError(
    message: String,
    cause: Throwable? = null,
) : InternalCheckoutError(message, cause)
