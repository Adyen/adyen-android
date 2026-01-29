/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/12/2025.
 */

package com.adyen.checkout.core.error.internal

// TODO - Platform alignment: Review error name and structure after iOS alignment.
/**
 * Errors related to encryption operations.
 *
 * These errors occur during card data encryption or other cryptographic operations.
 *
 * @param message A human-readable description of the error.
 * @param cause The underlying cause of this error, if any.
 */
class EncryptionError(
    message: String,
    cause: Throwable? = null,
) : InternalError(message, cause)
