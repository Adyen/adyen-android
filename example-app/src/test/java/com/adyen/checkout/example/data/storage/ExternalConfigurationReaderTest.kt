/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.example.data.storage

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Base64

internal class ExternalConfigurationReaderTest {

    private val reader = ExternalConfigurationReader()

    @Test
    fun `when applying valid config then showCardholderName override is set to true`() {
        val config = encodeBase64("""{"CARD_CONFIGURATION":{"showCardholderName":true}}""")

        reader.apply(config)

        assertEquals(true, reader.cardConfiguration?.showCardholderName)
    }

    @Test
    fun `when applying partial config then showCardholderName override is not set`() {
        val config = encodeBase64("""{"CARD_CONFIGURATION":{}}""")

        reader.apply(config)

        assertNull(reader.cardConfiguration?.showCardholderName)
    }

    @Test
    fun `when applying null config then override is cleared`() {
        reader.apply(encodeBase64("""{"CARD_CONFIGURATION":{"showCardholderName":true}}"""))

        reader.apply(null)

        assertNull(reader.cardConfiguration)
    }

    @Test
    fun `when applying empty config then override is cleared`() {
        reader.apply(encodeBase64("""{"CARD_CONFIGURATION":{"showCardholderName":true}}"""))

        reader.apply("")

        assertNull(reader.cardConfiguration)
    }

    @Test
    fun `when applying invalid base64 then override is not set`() {
        reader.apply("not-valid-base64!")

        assertNull(reader.cardConfiguration)
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
