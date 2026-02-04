/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/2/2026.
 */

package com.adyen.checkout.core.common.internal.helper

import com.adyen.checkout.core.error.CheckoutError

/**
 * Validator for client keys.
 */
internal object ClientKeyValidator {

    private const val MIN_LENGTH = 13
    private const val MAX_LENGTH = 137

    // Pattern: 4-8 lowercase letters, underscore, 8-128 alphanumeric characters
    private val CLIENT_KEY_REGEX = Regex("^[a-z]{4,8}_[a-zA-Z0-9]{8,128}$")

    /**
     * Validates a client key and returns a result containing either the valid key or an error.
     *
     * A valid client key must:
     * - Be between 13 and 137 characters long
     * - Match the pattern: 4-8 lowercase letters, underscore, 8-128 alphanumeric characters
     *
     * @param clientKey The client key to validate.
     * @return [ClientKeyValidationResult.Valid] if the key is valid, [ClientKeyValidationResult.Invalid] otherwise.
     */
    fun validateClientKey(clientKey: String): ClientKeyValidationResult = when {
        clientKey.length !in MIN_LENGTH..MAX_LENGTH -> {
            ClientKeyValidationResult.Invalid(
                CheckoutError(
                    code = CheckoutError.ErrorCode.INVALID_CLIENT_KEY,
                    message = "Invalid client key: length must be between $MIN_LENGTH and $MAX_LENGTH characters.",
                ),
            )
        }

        !CLIENT_KEY_REGEX.matches(clientKey) -> {
            ClientKeyValidationResult.Invalid(
                CheckoutError(
                    code = CheckoutError.ErrorCode.INVALID_CLIENT_KEY,
                    message = "Invalid client key: does not match expected format.",
                ),
            )
        }

        else -> ClientKeyValidationResult.Valid(clientKey)
    }
}

/**
 * Result of client key validation.
 */
internal sealed class ClientKeyValidationResult {
    data class Valid(val clientKey: String) : ClientKeyValidationResult()
    data class Invalid(val error: CheckoutError) : ClientKeyValidationResult()
}
