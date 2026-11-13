/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/11/2026.
 */

package com.adyen.checkout.cse.internal

import com.adyen.checkout.cse.Card
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.Encryptor
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.Calendar
import java.util.TimeZone

@ExtendWith(MockitoExtension::class)
internal class DefaultCheckoutEncryptorTest(
    @param:Mock private val dateGenerator: DateGenerator,
    @param:Mock private val jsonWebEncryptor: JSONWebEncryptor,
) {

    private val checkoutEncryptor = DefaultCheckoutEncryptor(
        DefaultGenericEncryptor(dateGenerator, jsonWebEncryptor),
    )

    @Nested
    inner class EncryptCardTest {

        @Test
        fun `when all card fields are set then all fields are encrypted with their payload keys`() {
            // GIVEN
            givenEncryptionSucceeds()
            val card = Card(
                number = "4111111111111111",
                expiryMonth = "03",
                expiryYear = "2030",
                securityCode = "737",
            )

            // WHEN
            val result = checkoutEncryptor.encrypt(card, PUBLIC_KEY)

            // THEN
            val expected = EncryptedCard(
                encryptedCardNumber = expectedEncryptedValue("number", "4111111111111111"),
                encryptedExpiryMonth = expectedEncryptedValue("expiryMonth", "03"),
                encryptedExpiryYear = expectedEncryptedValue("expiryYear", "2030"),
                encryptedSecurityCode = expectedEncryptedValue("cvc", "737"),
            )
            assertEquals(expected, result.getOrThrow())
        }

        @Test
        fun `when only security code is set then only security code is encrypted`() {
            // GIVEN
            givenEncryptionSucceeds()
            val card = Card(securityCode = "737")

            // WHEN
            val result = checkoutEncryptor.encrypt(card, PUBLIC_KEY)

            // THEN
            val expected = EncryptedCard(
                encryptedCardNumber = null,
                encryptedExpiryMonth = null,
                encryptedExpiryYear = null,
                encryptedSecurityCode = expectedEncryptedValue("cvc", "737"),
            )
            assertEquals(expected, result.getOrThrow())
        }

        @Test
        fun `when no card field is set then failure is returned without encrypting`() {
            // WHEN
            val result = checkoutEncryptor.encrypt(Card(), PUBLIC_KEY)

            // THEN
            assertError(Encryptor.Error.Code.INVALID_CARD, "Cannot encrypt a card without any fields set.", result)
            verifyNoInteractions(jsonWebEncryptor)
        }

        @Test
        fun `when card values contain whitespace, are empty or are not numeric then they are encrypted as they are`() {
            // GIVEN
            givenEncryptionSucceeds()
            val card = Card(
                number = " 4111 1111 1111 1111 ",
                expiryMonth = "",
                expiryYear = " ",
                securityCode = "73a",
            )

            // WHEN
            val result = checkoutEncryptor.encrypt(card, PUBLIC_KEY)

            // THEN
            val expected = EncryptedCard(
                encryptedCardNumber = expectedEncryptedValue("number", " 4111 1111 1111 1111 "),
                encryptedExpiryMonth = expectedEncryptedValue("expiryMonth", ""),
                encryptedExpiryYear = expectedEncryptedValue("expiryYear", " "),
                encryptedSecurityCode = expectedEncryptedValue("cvc", "73a"),
            )
            assertEquals(expected, result.getOrThrow())
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.cse.internal.DefaultCheckoutEncryptorTest#incompleteExpiryDateSource")
        fun `when only one of expiry month and expiry year is set then only the set fields are encrypted`(
            card: Card,
            expected: EncryptedCard,
        ) {
            // GIVEN
            givenEncryptionSucceeds()

            // WHEN
            val result = checkoutEncryptor.encrypt(card, PUBLIC_KEY)

            // THEN
            assertEquals(expected, result.getOrThrow())
        }

        @Test
        fun `when public key is invalid then failure is returned without encrypting`() {
            // WHEN
            val result = checkoutEncryptor.encrypt(Card(securityCode = "737"), "invalid")

            // THEN
            assertError(
                Encryptor.Error.Code.INVALID_PUBLIC_KEY,
                "Encryption failed because the public key is invalid.",
                result,
            )
            verifyNoInteractions(jsonWebEncryptor)
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.cse.internal.DefaultCheckoutEncryptorTest#encryptionExceptionSource")
        fun `when encryption throws then failure wrapping the cause is returned`(exception: Exception) {
            // GIVEN
            givenEncryptionThrows(exception)

            // WHEN
            val result = checkoutEncryptor.encrypt(Card(securityCode = "737"), PUBLIC_KEY)

            // THEN
            val error = assertError(Encryptor.Error.Code.ENCRYPTION_FAILED, "Encryption failed.", result)
            assertSame(exception, error.cause)
        }
    }

    @Nested
    inner class EncryptFieldTest {

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.cse.internal.DefaultCheckoutEncryptorTest#fieldKeySource")
        fun `when field is encrypted then its payload key is used`(field: Encryptor.Field, expectedKey: String) {
            // GIVEN
            givenEncryptionSucceeds()

            // WHEN
            val result = checkoutEncryptor.encrypt(field, "1234", PUBLIC_KEY)

            // THEN
            assertEquals(expectedEncryptedValue(expectedKey, "1234"), result.getOrThrow())
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.cse.internal.DefaultCheckoutEncryptorTest#unnormalizedValueSource")
        fun `when value contains whitespace, is empty or is not numeric then it is encrypted as it is`(
            value: String
        ) {
            // GIVEN
            givenEncryptionSucceeds()

            // WHEN
            val result = checkoutEncryptor.encrypt(Encryptor.Field.Bin, value, PUBLIC_KEY)

            // THEN
            assertEquals(expectedEncryptedValue("binValue", value), result.getOrThrow())
        }

        @Test
        fun `when public key is invalid then failure is returned without encrypting`() {
            // WHEN
            val result = checkoutEncryptor.encrypt(Encryptor.Field.Bin, "411111", "invalid")

            // THEN
            assertError(
                Encryptor.Error.Code.INVALID_PUBLIC_KEY,
                "Encryption failed because the public key is invalid.",
                result,
            )
            verifyNoInteractions(jsonWebEncryptor)
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.cse.internal.DefaultCheckoutEncryptorTest#encryptionExceptionSource")
        fun `when encryption throws then failure wrapping the cause is returned`(exception: Exception) {
            // GIVEN
            givenEncryptionThrows(exception)

            // WHEN
            val result = checkoutEncryptor.encrypt(Encryptor.Field.Bin, "411111", PUBLIC_KEY)

            // THEN
            val error = assertError(Encryptor.Error.Code.ENCRYPTION_FAILED, "Encryption failed.", result)
            assertSame(exception, error.cause)
        }
    }

    private fun assertError(expectedCode: String, expectedMessage: String, result: Result<*>): Encryptor.Error {
        val error = assertInstanceOf(Encryptor.Error::class.java, result.exceptionOrNull())
        assertEquals(expectedCode, error.code)
        assertEquals(expectedMessage, error.message)
        return error
    }

    private fun givenEncryptionSucceeds() {
        whenever(dateGenerator.getCurrentDate()) doReturn DATE
        whenever(jsonWebEncryptor.encrypt(any(), any())).thenAnswer {
            "${it.arguments[0]}-${it.arguments[1]}"
        }
    }

    private fun givenEncryptionThrows(exception: Exception) {
        whenever(dateGenerator.getCurrentDate()) doReturn DATE
        whenever(jsonWebEncryptor.encrypt(any(), any())).thenThrow(exception)
    }

    companion object {
        private val PUBLIC_KEY = "10001|${"A".repeat(512)}"

        private val DATE = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.NOVEMBER)
            set(Calendar.DAY_OF_MONTH, 4)
            set(Calendar.HOUR_OF_DAY, 16)
            set(Calendar.MINUTE, 13)
            set(Calendar.SECOND, 45)
            set(Calendar.MILLISECOND, 123)
        }.time

        private const val FORMATTED_DATE = "2026-11-04T16:13:45.123Z"

        private fun expectedEncryptedValue(key: String, value: String): String {
            val plainText = JSONObject().apply {
                put(key, value)
                put("generationtime", FORMATTED_DATE)
            }.toString()
            return "$PUBLIC_KEY-$plainText"
        }

        @JvmStatic
        fun incompleteExpiryDateSource() = listOf(
            arguments(
                Card(expiryMonth = "03"),
                EncryptedCard(
                    encryptedCardNumber = null,
                    encryptedExpiryMonth = expectedEncryptedValue("expiryMonth", "03"),
                    encryptedExpiryYear = null,
                    encryptedSecurityCode = null,
                ),
            ),
            arguments(
                Card(expiryYear = "2030"),
                EncryptedCard(
                    encryptedCardNumber = null,
                    encryptedExpiryMonth = null,
                    encryptedExpiryYear = expectedEncryptedValue("expiryYear", "2030"),
                    encryptedSecurityCode = null,
                ),
            ),
            arguments(
                Card(number = "4111111111111111", expiryMonth = "03", securityCode = "737"),
                EncryptedCard(
                    encryptedCardNumber = expectedEncryptedValue("number", "4111111111111111"),
                    encryptedExpiryMonth = expectedEncryptedValue("expiryMonth", "03"),
                    encryptedExpiryYear = null,
                    encryptedSecurityCode = expectedEncryptedValue("cvc", "737"),
                ),
            ),
        )

        @JvmStatic
        fun encryptionExceptionSource() = listOf(
            arguments(IllegalArgumentException("Invalid public key")),
            arguments(IllegalStateException("Unexpected")),
        )

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

        @JvmStatic
        fun unnormalizedValueSource() = listOf(
            arguments(" 4111 11 "),
            arguments(""),
            arguments(" "),
            arguments("41a1"),
        )
    }
}
