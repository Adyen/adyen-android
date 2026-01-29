/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/12/2025.
 */

package com.adyen.checkout.core.error.internal

// TODO - Remove this class once specific error types (ApiError, EncryptionError, etc.) are implemented.
//  This is a temporary catch-all error class used during the error framework migration.
/**
 * A generic internal error that occurred within a component.
 *
 * This error is used for internal SDK errors that don't fit into more specific categories.
 *
 * @param message A human-readable description of the error.
 * @param cause The underlying cause of this error, if any.
 */
class ComponentError(
    message: String,
    cause: Throwable? = null,
) : InternalError(message, cause)
