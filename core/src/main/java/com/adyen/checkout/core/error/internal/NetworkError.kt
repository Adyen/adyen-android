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
 * Errors related to network operations and API calls.
 *
 * These errors occur when there are issues with network connectivity,
 * API responses, or communication with Adyen servers.
 *
 * @param message A human-readable description of the error.
 * @param cause The underlying cause of this error, if any.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class NetworkError(
    message: String,
    cause: Throwable? = null,
) : InternalCheckoutError(message, cause)
