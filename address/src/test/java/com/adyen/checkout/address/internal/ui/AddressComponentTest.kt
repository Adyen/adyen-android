/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui

import app.cash.turbine.test
import com.adyen.checkout.address.internal.ui.model.AddressComponentParams
import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.address.internal.ui.view.AddressSecondaryContentEntry
import com.adyen.checkout.core.components.internal.ui.SecondaryNavigationEvent
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
internal class AddressComponentTest {

    @Test
    fun `when the country field is clicked, then the country picker is opened`() = runTest {
        // GIVEN
        val component = createComponent()

        component.navigation.test {
            // WHEN
            component.onCountryPickerClick()

            // THEN
            assertEquals(SecondaryNavigationEvent.Open(AddressSecondaryContentEntry.COUNTRY_PICKER), awaitItem())
        }
    }

    @Test
    fun `when a country is picked, then the country picker is closed`() = runTest {
        // GIVEN
        val component = createComponent()

        component.navigation.test {
            // WHEN
            component.onCountrySelected(NETHERLANDS)

            // THEN
            assertEquals(SecondaryNavigationEvent.Close, awaitItem())
        }
    }

    @Test
    fun `when a country is picked and the address is confirmed, then the confirmed address has that country`() =
        runTest {
            // GIVEN
            val component = createComponent()
            component.prefill(AddressModel(country = "US", postalCode = "12345"))
            component.onCountrySelected(NETHERLANDS)

            component.eventFlow.test {
                // WHEN
                component.confirm()

                // THEN
                assertEquals(
                    AddressComponentEvent.Confirmed(AddressModel(country = "NL", postalCode = "12345")),
                    awaitItem(),
                )
            }
        }

    @Test
    fun `when a prefilled address is confirmed, then the prefilled address is confirmed`() = runTest {
        // GIVEN
        val component = createComponent()
        val address = AddressModel(country = "NL", postalCode = "1234 AB")
        component.prefill(address)

        component.eventFlow.test {
            // WHEN
            component.confirm()

            // THEN
            assertEquals(AddressComponentEvent.Confirmed(address), awaitItem())
        }
    }

    @Test
    fun `when an incomplete address is confirmed, then nothing is confirmed`() = runTest {
        // GIVEN
        val component = createComponent()

        component.eventFlow.test {
            // WHEN
            component.confirm()

            // THEN
            expectNoEvents()
        }
    }

    @Test
    fun `when the form is prefilled without an address, then earlier values are dropped`() = runTest {
        // GIVEN
        val component = createComponent()
        component.prefill(AddressModel(country = "NL", postalCode = "1234 AB"))

        // WHEN
        component.prefill(address = null)

        // THEN
        component.eventFlow.test {
            component.confirm()
            expectNoEvents()
        }
    }

    private fun createComponent() = AddressComponentFactory().create(
        componentParams = AddressComponentParams(
            shopperLocale = Locale.forLanguageTag("en-US"),
            supportedCountryCodes = setOf("NL", "US"),
        ),
        coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
    )

    companion object {
        private val NETHERLANDS = CountryModel(isoCode = "NL", countryName = "Netherlands", callingCode = "+31")
    }
}
