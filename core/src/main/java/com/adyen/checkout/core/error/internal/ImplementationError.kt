/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/12/2025.
 */

package com.adyen.checkout.core.error.internal

import androidx.annotation.RestrictTo

/**
 * Errors caused by incorrect SDK integration or configuration.
 *
 * These errors indicate that the merchant has misconfigured the SDK,
 * passed invalid parameters, or failed to implement required callbacks.
 * These typically require code changes to fix.
 *
 * @param message A human-readable description of the error.
 * @param cause The underlying cause of this error, if any.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class ImplementationError(
    message: String,
    cause: Throwable? = null,
) : InternalCheckoutError(message, cause)
