/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/1/2026.
 */

package com.adyen.checkout.core.error

/**
 * Represents an error that occurred during the checkout process.
 *
 * @param code A unique code identifying the type of error. See [ErrorCode] for standard codes.
 * @param message A human-readable description of the error.
 * @param cause The underlying exception that caused this error, if any.
 */
data class CheckoutError(
    val code: String,
    val message: String?,
    val cause: Throwable? = null,
) {

    /**
     * Standard error codes for checkout errors.
     */
    object ErrorCode {
        // Checkout Configuration Errors
        const val INVALID_CLIENT_KEY = "InvalidClientKey"
        const val INVALID_LOCALE = "InvalidLocale"
        const val INVALID_CURRENCY_CODE = "InvalidCurrencyCode"
        const val INVALID_AMOUNT_VALUE = "InvalidAmountValue"

        // Session Setup Errors
        const val SESSION_SETUP_FAILURE = "SessionSetupFailure"

        // Unknown exceptions
        // TODO - Error propagation - This should be removed after all error codes are implemented
        internal const val UNKNOWN = "Unknown"
    }
}
