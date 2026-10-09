/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 9/10/2026.
 */

package com.adyen.checkout.address.internal.ui.properties

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class PostalCodePropertiesTest {

    @ParameterizedTest
    @MethodSource("maxLengthSource")
    fun `when the country has a maximum then it is used`(countryCode: String, expected: Int) {
        // WHEN
        val maxLength = PostalCodeProperties.getMaxLength(countryCode)

        // THEN
        assertEquals(expected, maxLength)
    }

    @Test
    fun `when the country has no maximum then the maximum is ten`() {
        // WHEN
        val maxLength = PostalCodeProperties.getMaxLength("AR")

        // THEN
        assertEquals(10, maxLength)
    }

    @Test
    fun `when no country is selected then the maximum is ten`() {
        // WHEN
        val maxLength = PostalCodeProperties.getMaxLength(null)

        // THEN
        assertEquals(10, maxLength)
    }

    /** The API does not accept a postal code longer than ten characters, whatever the country. */
    @Test
    fun `when any country is asked then the maximum is at most ten`() {
        // WHEN
        val maxLengths = Locale.getISOCountries().map { countryCode -> PostalCodeProperties.getMaxLength(countryCode) }

        // THEN
        assertTrue(maxLengths.all { it <= 10 })
    }

    companion object {

        @JvmStatic
        fun maxLengthSource() = listOf(
            arguments("NL", 7),
            arguments("US", 10),
            arguments("IS", 3),
            arguments("BR", 9),
            arguments("GE", 4),
        )
    }
}
