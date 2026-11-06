/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/11/2026.
 */

package com.adyen.checkout.cse

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class EncryptorTest {

    @ParameterizedTest
    @MethodSource("fieldKeySource")
    fun `when field is used then its key matches the payload key`(field: Encryptor.Field, expectedKey: String) {
        // THEN
        assertEquals(expectedKey, field.key)
    }

    @Test
    fun `when error is created then code, message and cause are kept`() {
        // GIVEN
        val cause = IllegalStateException("cause")

        // WHEN
        val error = Encryptor.Error(Encryptor.Error.Code.ENCRYPTION_FAILED, "message", cause)

        // THEN
        assertEquals(Encryptor.Error.Code.ENCRYPTION_FAILED, error.code)
        assertEquals("message", error.message)
        assertSame(cause, error.cause)
    }

    @Test
    fun `when card is encrypted with an invalid public key then failure is returned`() {
        // WHEN
        val result = Encryptor.encrypt(Card(securityCode = "737"), "invalid")

        // THEN
        assertInstanceOf(Encryptor.Error::class.java, result.exceptionOrNull())
    }

    companion object {

        @JvmStatic
        fun fieldKeySource() = listOf(
            arguments(Encryptor.Field.CardNumber, "number"),
            arguments(Encryptor.Field.ExpiryMonth, "expiryMonth"),
            arguments(Encryptor.Field.ExpiryYear, "expiryYear"),
            arguments(Encryptor.Field.SecurityCode, "cvc"),
            arguments(Encryptor.Field.Bin, "binValue"),
            arguments(Encryptor.Field.KcpPassword, "password"),
            arguments(Encryptor.Field.BankAccountNumber, "bankAccountNumber"),
            arguments(Encryptor.Field.BankRoutingNumber, "bankLocationId"),
        )
    }
}
