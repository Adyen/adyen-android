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
 * Sealed base class for all internal SDK errors.
 *
 * All internal error types extend this class directly. Being sealed ensures exhaustive handling
 * in `when` expressions, so adding a new error type forces a compiler error at all handling sites.
 *
 * @param message A human-readable description of the error.
 * @param cause The underlying cause of this error, if any.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class InternalCheckoutError(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
