/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/2/2026.
 */

package com.adyen.checkout.core.common.internal.helper

import com.adyen.checkout.core.error.CheckoutError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ClientKeyValidatorTest {

    @ParameterizedTest
    @MethodSource("validClientKeysSource")
    fun `when client key is valid then return Valid result`(clientKey: String) {
        val result = ClientKeyValidator.validateClientKey(clientKey)

        assertTrue(result is ClientKeyValidationResult.Valid)
        assertEquals(clientKey, (result as ClientKeyValidationResult.Valid).clientKey)
    }

    @ParameterizedTest
    @MethodSource("invalidClientKeysSource")
    fun `when client key is invalid then return Invalid result`(clientKey: String) {
        val result = ClientKeyValidator.validateClientKey(clientKey)

        assertTrue(result is ClientKeyValidationResult.Invalid)
        assertEquals(
            CheckoutError.ErrorCode.INVALID_CLIENT_KEY,
            (result as ClientKeyValidationResult.Invalid).error.code,
        )
    }

    @Test
    fun `when client key is too short then return Invalid result with length error`() {
        val result = ClientKeyValidator.validateClientKey("test_1234567")

        assertTrue(result is ClientKeyValidationResult.Invalid)
        val error = (result as ClientKeyValidationResult.Invalid).error
        assertEquals(CheckoutError.ErrorCode.INVALID_CLIENT_KEY, error.code)
        assertTrue(error.message?.contains("length") == true)
    }

    @Test
    fun `when client key is too long then return Invalid result`() {
        // 138 chars total exceeds MAX_LENGTH of 137
        val longKey = "testtest_" + "a".repeat(129)
        val result = ClientKeyValidator.validateClientKey(longKey)

        assertTrue(result is ClientKeyValidationResult.Invalid)
        val error = (result as ClientKeyValidationResult.Invalid).error
        assertEquals(CheckoutError.ErrorCode.INVALID_CLIENT_KEY, error.code)
    }

    companion object {

        @JvmStatic
        fun validClientKeysSource() = listOf(
            // Minimum valid: 4 lowercase + underscore + 8 alphanumeric = 13 chars
            arguments("test_12345678"),
            // Standard live key format
            arguments("live_ABCDEFGHIJKLMNOP"),
            // Standard test key format
            arguments("test_abcdefghijklmnop"),
            // Mixed alphanumeric after underscore
            arguments("test_AbCdEf12345678"),
            // 8 letter prefix (maximum)
            arguments("testtest_12345678"),
            // Long alphanumeric part
            arguments("test_" + "a".repeat(128)),
            // 4 letter prefix (minimum)
            arguments("test_12345678"),
            // 5 letter prefix
            arguments("tests_12345678"),
            // 6 letter prefix
            arguments("testss_12345678"),
            // 7 letter prefix
            arguments("testsss_12345678"),
        )

        @JvmStatic
        fun invalidClientKeysSource() = listOf(
            // Empty string
            arguments(""),
            // No underscore
            arguments("test12345678"),
            // Uppercase in prefix
            arguments("Test_12345678"),
            // Prefix too short (3 chars)
            arguments("tes_12345678"),
            // Prefix too long (9 chars)
            arguments("testtests_12345678"),
            // Special characters in prefix
            arguments("te-t_12345678"),
            // Numbers in prefix
            arguments("tes1_12345678"),
            // Special characters after underscore
            arguments("test_1234567!"),
            // Spaces
            arguments("test_1234 5678"),
            // Multiple underscores
            arguments("test__12345678"),
            // Underscore at start
            arguments("_test12345678"),
            // Only underscore
            arguments("_"),
            // Alphanumeric part too short (7 chars)
            arguments("test_1234567"),
        )
    }
}
