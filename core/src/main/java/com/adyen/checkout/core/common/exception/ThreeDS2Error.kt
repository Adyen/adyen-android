/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/12/2025.
 */

package com.adyen.checkout.core.common.exception

// TODO - Platform alignment: Review error name and structure after iOS alignment.
/**
 * Errors related to 3D Secure 2 authentication flow.
 *
 * These errors occur during the 3DS2 fingerprint or challenge process.
 *
 * @param errorCode A unique code identifying the specific error. See [ErrorCode] for possible values.
 * @param message A human-readable description of the error.
 * @param cause The underlying cause of this error, if any.
 */
class ThreeDS2Error(
    val errorCode: String,
    message: String,
    cause: Throwable? = null,
) : InternalError(message, cause) {

    /**
     * Error codes for 3DS2 errors.
     */
    object ErrorCode {
        /** The 3DS2 token is missing from the action. */
        const val TOKEN_MISSING = "tokenMissing"

        /** Failed to decode the 3DS2 token. */
        const val TOKEN_DECODING = "tokenDecoding"

        /** Failed to create the 3DS2 fingerprint. */
        const val FINGERPRINT_CREATION = "fingerprintCreation"

        /** Failed to create the 3DS2 transaction. */
        const val TRANSACTION_CREATION = "transactionCreation"

        /** The 3DS2 transaction is missing. */
        const val TRANSACTION_MISSING = "transactionMissing"

        /** Error while handling the 3DS2 fingerprint. */
        const val FINGERPRINT_HANDLING = "fingerprintHandling"

        /** Error while handling the 3DS2 challenge. */
        const val CHALLENGE_HANDLING = "challengeHandling"
    }
}
