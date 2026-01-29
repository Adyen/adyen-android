/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/12/2025.
 */

package com.adyen.checkout.core.error.internal

/**
 * Base class for internal SDK errors.
 *
 * This is the root of the error hierarchy that merchants receive through callbacks.
 * Use specific subclasses to handle different error scenarios.
 *
 * ## Error Hierarchy
 *
 * - [UserError] - Errors caused by user actions (cancellation, denial)
 * - [NetworkError] - Network and API related errors
 * - [ImplementationError] - Merchant integration issues
 * - [InternalError] - SDK internal errors
 *
 * ## Usage
 *
 * ```kotlin
 * override fun onError(error: CheckoutError) {
 *     when (error) {
 *         is UserError -> // Handle user-initiated error
 *         is NetworkError -> // Handle network error
 *         is ImplementationError -> // Handle integration error
 *         is InternalError -> // Handle internal error
 *         is ... // Handle more granular error types
 *         else -> // Handle unknown error
 *     }
 * }
 * ```
 *
 * @param message A human-readable description of the error.
 * @param cause The underlying cause of this error, if any.
 */
// TODO - Error propagation [COSDK-981] - Make this InternalCheckoutError
abstract class CheckoutError(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
