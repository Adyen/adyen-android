/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/12/2025.
 */

package com.adyen.checkout.core.error.internal

import androidx.annotation.RestrictTo

// TODO - Platform alignment: Review error name and structure after iOS alignment.
/**
 * Errors related to redirect actions.
 *
 * These errors occur during redirect-based payment flows.
 *
 * @param errorCode A unique code identifying the specific error. See [ErrorCode] for possible values.
 * @param message A human-readable description of the error.
 * @param cause The underlying cause of this error, if any.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class RedirectError(
    val errorCode: String,
    message: String,
    cause: Throwable? = null,
) : InternalError(message, cause) {

    /**
     * Error codes for redirect errors.
     */
    object ErrorCode {
        /** The redirect action failed to launch. */
        const val REDIRECT_FAILED = "redirectFailed"

        /** Failed to parse the redirect result. */
        const val REDIRECT_PARSE_FAILED = "redirectParseFailed"
    }
}
