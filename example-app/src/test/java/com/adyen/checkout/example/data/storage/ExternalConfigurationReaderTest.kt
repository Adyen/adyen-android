/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.example.data.storage

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.util.Base64

internal class ExternalConfigurationReaderTest {

    private val keyValueStorage = mock<KeyValueStorage>()
    private val reader = ExternalConfigurationReader(keyValueStorage)

    @Test
    fun `when applying valid config then showCardholderName is set to true`() {
        val config = encodeBase64("""{"CARD_CONFIGURATION":{"showCardholderName":true}}""")

        reader.apply(config)

        verify(keyValueStorage).setShowCardholderName(true)
    }

    @Test
    fun `when applying partial config then showCardholderName is not set`() {
        val config = encodeBase64("""{"CARD_CONFIGURATION":{}}""")

        reader.apply(config)

        verify(keyValueStorage, never()).setShowCardholderName(any())
    }

    @Test
    fun `when applying null config then showCardholderName is reset to default`() {
        reader.apply(null)

        verify(keyValueStorage).setShowCardholderName(SettingsDefaults.SHOW_CARDHOLDER_NAME)
    }

    @Test
    fun `when applying empty config then showCardholderName is reset to default`() {
        reader.apply("")

        verify(keyValueStorage).setShowCardholderName(SettingsDefaults.SHOW_CARDHOLDER_NAME)
    }

    @Test
    fun `when applying invalid base64 then showCardholderName is not set`() {
        reader.apply("not-valid-base64!")

        verify(keyValueStorage, never()).setShowCardholderName(any())
    }

    @Test
    fun `fromJson parses card configuration with showCardholderName`() {
        val config = ExternalConfiguration.fromJson("""{"CARD_CONFIGURATION":{"showCardholderName":true}}""")

        assertEquals(true, config.card?.showCardholderName)
    }

    @Test
    fun `fromJson parses empty card configuration with null showCardholderName`() {
        val config = ExternalConfiguration.fromJson("""{"CARD_CONFIGURATION":{}}""")

        assertNull(config.card?.showCardholderName)
    }

    @Test
    fun `fromJson without card configuration returns null card`() {
        val config = ExternalConfiguration.fromJson("{}")

        assertNull(config.card)
    }

    private fun encodeBase64(payload: String): String = Base64.getEncoder().encodeToString(payload.toByteArray())
}
