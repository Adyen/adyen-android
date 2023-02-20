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
class DefaultCardEncrypterTest(
    @Mock private val clientSideEncrypter: ClientSideEncrypter,
    @Mock private val dateGenerator: DateGenerator,
) {
    private val cardEncrypter = DefaultCardEncrypter(
        DefaultGenericEncrypter(clientSideEncrypter, dateGenerator),
    )

    @BeforeEach
    fun setup() {
        whenever(dateGenerator.getCurrentDate()) doReturn DATE
        whenever(clientSideEncrypter.encrypt(any(), any())).thenAnswer {
            "${it.arguments[0]}-${it.arguments[1]}"
        }
    }

    @Test
    fun `when encryptFields is called with UnencryptedCard then correct EncryptedCard is returned`() {
        val number = "1234123412341234"
        val expiryYear = "12"
        val expiryMonth = "34"
        val cvc = "737"
        val publicKey = "PUBLIC_KEY"

        val unencryptedCard = UnencryptedCard.Builder()
            .setNumber(number)
            .setExpiryDate(expiryMonth, expiryYear)
            .setCvc(cvc)
            .build()

        val encryptedCard = cardEncrypter.encryptFields(unencryptedCard, publicKey)

        val expectedNumberPlainText = JSONObject().apply {
            put("number", number)
            put("generationtime", FORMATTED_DATE)
        }.toString()

        val expectedExpiryYearText = JSONObject().apply {
            put("expiryYear", expiryYear)
            put("generationtime", FORMATTED_DATE)
        }.toString()

        val expectedExpiryMonthText = JSONObject().apply {
            put("expiryMonth", expiryMonth)
            put("generationtime", FORMATTED_DATE)
        }.toString()

        val expectedCVCPlainText = JSONObject().apply {
            put("cvc", cvc)
            put("generationtime", FORMATTED_DATE)
        }.toString()

        val expectedEncryptedCard = EncryptedCard(
            encryptedCardNumber = "$publicKey-$expectedNumberPlainText",
            encryptedExpiryMonth = "$publicKey-$expectedExpiryMonthText",
            encryptedExpiryYear = "$publicKey-$expectedExpiryYearText",
            encryptedSecurityCode = "$publicKey-$expectedCVCPlainText",
        )

        assertEquals(expectedEncryptedCard, encryptedCard)
    }

    @Test
    fun `when encrypt is called with UnencryptedCard then correct encrypted string is returned`() {
        val number = "1234123412341234"
        val expiryYear = "12"
        val expiryMonth = "34"
        val cvc = "737"
        val holderName = "HOLDER_NAME"
        val publicKey = "PUBLIC_KEY"

        val unencryptedCard = UnencryptedCard.Builder()
            .setNumber(number)
            .setExpiryDate(expiryMonth, expiryYear)
            .setCvc(cvc)
            .setHolderName(holderName)
            .build()

        val encryptedCard = cardEncrypter.encrypt(unencryptedCard, publicKey)

        val expectedPlainText = JSONObject().apply {
            put("number", number)
            put("expiryMonth", expiryMonth)
            put("expiryYear", expiryYear)
            put("cvc", cvc)
            put("holderName", holderName)
            put("generationtime", FORMATTED_DATE)
        }.toString()

        assertEquals(encryptedCard, "$publicKey-$expectedPlainText")
    }

    @Test
    fun `when encryptBin is called with UnencryptedCard then correct encrypted bin is returned`() {
        val bin = "123412341234"
        val publicKey = "PUBLIC_KEY"

        val encryptedCard = cardEncrypter.encryptBin(bin, publicKey)

        val expectedPlainText = JSONObject().apply {
            put("binValue", bin)
            put("generationtime", FORMATTED_DATE)
        }.toString()

        assertEquals(encryptedCard, "$publicKey-$expectedPlainText")
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
