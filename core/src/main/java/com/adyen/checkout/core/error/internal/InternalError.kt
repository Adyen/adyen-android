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
 * Errors caused by internal SDK operations.
 *
 * These errors occur during internal SDK operations such as encryption,
 * serialization, payment method availability checks, or authentication flows.
 *
 * @param message A human-readable description of the error.
 * @param cause The underlying cause of this error, if any.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class InternalError(
    message: String,
    cause: Throwable? = null,
) : InternalCheckoutError(message, cause)
