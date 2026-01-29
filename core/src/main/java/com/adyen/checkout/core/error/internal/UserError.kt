/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/12/2025.
 */

package com.adyen.checkout.core.error.internal

/**
 * Errors caused by explicit user actions.
 *
 * These errors occur when the user intentionally interrupts or cancels the payment flow,
 * such as pressing a back button, denying permissions, or declining a payment.
 *
 * @param message A human-readable description of the error.
 * @param cause The underlying cause of this error, if any.
 */
abstract class UserError(
    message: String,
    cause: Throwable? = null,
) : CheckoutError(message, cause)
