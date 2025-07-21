/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.util

import com.adyen.checkout.ui.core.old.internal.ui.model.CountryModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale
import com.adyen.checkout.components.core.internal.util.CountryUtils as CoreCountryUtils

internal class CountryUtilsTest {

    @Test
    fun `when passing nothing, then all countries are returned`() {
        val actual = CountryUtils.getLocalizedCountries(Locale.US)

        val expected = CoreCountryUtils.getCountries().map {
            CountryModel(it.isoCode, CoreCountryUtils.getCountryName(it.isoCode, Locale.US), it.callingCode)
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
        val actual =
            CountryUtils.getLocalizedCountries(Locale.US, specifiedCountries, compareByDescending { it.isoCode })

        val expected = listOf(
            CountryModel("US", "United States", "+1"),
            CountryModel("NL", "Netherlands", "+31"),
            CountryModel("DE", "Germany", "+49"),
        )
        assertEquals(expected, actual)
    }
}
