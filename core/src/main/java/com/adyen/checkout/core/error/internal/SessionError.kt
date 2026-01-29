/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 31/12/2025.
 */

package com.adyen.checkout.core.error.internal

/**
 * An error that occurred during a session API call.
 *
 * This error wraps exceptions that occur when making payments or details calls
 * through the sessions flow.
 *
 * @param message A human-readable description of the error.
 * @param cause The underlying cause of this error.
 */
class SessionError(
    message: String,
    cause: Throwable? = null,
) : InternalError(message, cause)
