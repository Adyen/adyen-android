/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 30/1/2026.
 */

package com.adyen.checkout.core.error

/**
 * Exception thrown when a checkout error occurs that cannot be delivered via callback.
 *
 * This is typically used for errors that happen during implementation and
 * before any callbacks are available.
 *
 * @param error The [CheckoutError] containing details about what went wrong.
 */
class CheckoutException(
    val error: CheckoutError
) : Exception(error.message, error.cause)
