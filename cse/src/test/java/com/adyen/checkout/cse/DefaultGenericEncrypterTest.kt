/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/2/2023.
 */

package com.adyen.checkout.cse

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.Calendar
import java.util.TimeZone

@ExtendWith(MockitoExtension::class)
internal class DefaultGenericEncrypterTest(
    @Mock private val clientSideEncrypter: ClientSideEncrypter,
    @Mock private val dateGenerator: DateGenerator,
) {
    private val genericEncrypter = DefaultGenericEncrypter(clientSideEncrypter, dateGenerator)

    @BeforeEach
    fun setup() {
        whenever(dateGenerator.getCurrentDate()) doReturn DATE
        whenever(clientSideEncrypter.encrypt(any(), any())).thenAnswer {
            "${it.arguments[0]}-${it.arguments[1]}"
        }
    }

    @Test
    fun `when encrypt field is called then correct encrypted string is returned`() {
        val key = "KEY"
        val value = "VALUE"
        val publicKey = "PUBLIC_KEY"

        val encrypted = genericEncrypter.encryptField(
            encryptionKey = key,
            fieldToEncrypt = value,
            publicKey = publicKey
        )

        val expectedPlainText = JSONObject().apply {
            put(key, value)
            put("generationtime", FORMATTED_DATE)
        }.toString()

        assertEquals("$publicKey-$expectedPlainText", encrypted)
    }

    companion object {
        private val DATE = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2023)
            set(Calendar.MONTH, Calendar.FEBRUARY)
            set(Calendar.DAY_OF_MONTH, 17)
            set(Calendar.HOUR_OF_DAY, 16)
            set(Calendar.MINUTE, 13)
            set(Calendar.SECOND, 45)
            set(Calendar.MILLISECOND, 123)
            timeZone = TimeZone.getTimeZone("UTC")
        }.time

        private const val FORMATTED_DATE = "2023-02-17T16:13:45.123Z"
    }
}
