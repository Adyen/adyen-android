/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 20/11/2026.
 */

package com.adyen.checkout.cse.internal

import com.adyen.checkout.cse.Encryptor
import org.json.JSONException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Calendar
import java.util.TimeZone

internal class EncryptionPlainTextGeneratorTest {

    @Test
    fun `when entries are valid then plain text contains the entries and the generation time`() {
        // WHEN
        val plainText = EncryptionPlainTextGenerator.generate(DATE, mapOf("KEY" to "VALUE"))

        // THEN
        val expected = JSONObject().apply {
            put("KEY", "VALUE")
            put("generationtime", FORMATTED_DATE)
        }.toString()
        assertEquals(expected, plainText)
    }

    @Test
    fun `when an entry cannot be converted to JSON then an encryption failed error is thrown`() {
        // WHEN
        val error = assertThrows<Encryptor.Error> {
            EncryptionPlainTextGenerator.generate(DATE, mapOf("KEY" to Double.NaN))
        }

        // THEN
        assertEquals(Encryptor.Error.Code.ENCRYPTION_FAILED, error.code)
        assertEquals("Encryption failed.", error.message)
        assertInstanceOf(JSONException::class.java, error.cause)
    }

    companion object {
        private val DATE = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.NOVEMBER)
            set(Calendar.DAY_OF_MONTH, 20)
            set(Calendar.HOUR_OF_DAY, 16)
            set(Calendar.MINUTE, 13)
            set(Calendar.SECOND, 45)
            set(Calendar.MILLISECOND, 123)
        }.time

        private const val FORMATTED_DATE = "2026-11-20T16:13:45.123Z"
    }
}
