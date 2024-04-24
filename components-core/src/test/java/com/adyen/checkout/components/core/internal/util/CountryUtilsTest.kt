package com.adyen.checkout.components.core.internal.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Locale

internal class CountryUtilsTest {

    @Nested
    inner class GetCountriesTest {

        @Test
        fun `when passing nothing, then all countries are returned`() {
            val actual = CountryUtils.getCountries()

            assertEquals(CountryUtils.countries, actual)
        }

        @Test
        fun `when passing list of countries, then only specified countries are returned`() {
            val specifiedCountries = listOf(
                "NL",
                "US",
                "DE",
            )
            val actual = CountryUtils.getCountries(specifiedCountries)

            val expected = listOf(
                CountryInfo(isoCode = "DE", callingCode = "+49"),
                CountryInfo(isoCode = "NL", callingCode = "+31"),
                CountryInfo(isoCode = "US", callingCode = "+1"),
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `when getting country name, then it is returned localized`() {
        val actual = CountryUtils.getCountryName("NL", Locale.US)

        val expected = "Netherlands"
        assertEquals(expected, actual)
    }
}
