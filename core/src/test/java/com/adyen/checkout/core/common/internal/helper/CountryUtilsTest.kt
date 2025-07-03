/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 30/6/2025.
 */

package com.adyen.checkout.core.common.internal.helper

import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Locale

internal class CountryUtilsTest {

    @Test
    fun `when passing nothing, then all countries are returned`() {
        val actual = CountryUtils.getLocalizedCountries(Locale.US)

        val expected = CountryUtils.getCountries().map {
            CountryModel(
                it.isoCode,
                CountryUtils.getCountryName(it.isoCode, Locale.US),
                it.callingCode
            )
        }.sortedBy { it.countryName }
        assertEquals(expected, actual)
    }

    @Test
    fun `when passing list of countries, then only specified countries are returned`() {
        val specifiedCountries = listOf(
            "NL",
            "US",
            "DE",
        )
        val actual = CountryUtils.getLocalizedCountries(Locale.US, specifiedCountries)

        val expected = listOf(
            CountryModel("DE", "Germany", "+49"),
            CountryModel("NL", "Netherlands", "+31"),
            CountryModel("US", "United States", "+1"),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `when passing sorting, then result is sorted correctly`() {
        val specifiedCountries = listOf(
            "NL",
            "US",
            "DE",
        )
        val actual = CountryUtils.getLocalizedCountries(
            Locale.US,
            specifiedCountries,
            compareByDescending { it.isoCode }
        )

        val expected = listOf(
            CountryModel("US", "United States", "+1"),
            CountryModel("NL", "Netherlands", "+31"),
            CountryModel("DE", "Germany", "+49"),
        )
        assertEquals(expected, actual)
    }

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
