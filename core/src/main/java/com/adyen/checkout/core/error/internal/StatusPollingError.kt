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
 * Errors related to status polling operations.
 *
 * These errors occur during polling-based payment flows such as await actions.
 *
 * @param message A human-readable description of the error.
 * @param cause The underlying cause of this error, if any.
 */
class StatusPollingError(
    message: String,
    cause: Throwable? = null,
) : InternalError(message, cause)
