/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui

import app.cash.turbine.test
import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.address.internal.ui.state.AddressFormElement
import com.adyen.checkout.address.internal.ui.view.AddressSecondaryContentEntry
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.internal.AnalyticsParams
import com.adyen.checkout.core.components.internal.AnalyticsParamsLevel
import com.adyen.checkout.core.components.internal.ui.SecondaryNavigationEvent
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
internal class AddressComponentTest {

    @Test
    fun `when the form is shown, then the form is opened`() = runTest {
        // GIVEN
        val component = createComponent()

        component.navigation.test {
            // WHEN
            component.show(address = null)

            // THEN
            assertEquals(SecondaryNavigationEvent.Open(AddressSecondaryContentEntry.FORM.key), awaitItem())
        }
    }

    @Test
    fun `when a valid address is confirmed, then the form is closed`() = runTest {
        // GIVEN
        val component = createComponent()
        component.show(AddressModel(country = "NL", postalCode = "1234 AB"))

        component.navigation.test {
            skipItems(1)

            // WHEN
            component.confirm()

            // THEN
            assertEquals(SecondaryNavigationEvent.Close, awaitItem())
        }
    }

    @Test
    fun `when an incomplete address is confirmed, then the form stays open`() = runTest {
        // GIVEN
        val component = createComponent()
        component.show(address = null)

        component.navigation.test {
            skipItems(1)

            // WHEN
            component.confirm()

            // THEN
            expectNoEvents()
        }
    }

    @Test
    fun `when asked about one of its own screens, then the component has it`() {
        // GIVEN
        val component = createComponent()

        // THEN
        AddressSecondaryContentEntry.entries.forEach { entry ->
            assertTrue(component.hasScreen(entry.key))
        }
    }

    @Test
    fun `when asked about a screen of its host, then the component does not have it`() {
        // GIVEN
        val component = createComponent()

        // THEN
        assertFalse(component.hasScreen("INSTALLMENTS"))
    }

    @Test
    fun `when the country field is clicked, then the country picker is opened`() = runTest {
        // GIVEN
        val component = createComponent()

        component.navigation.test {
            // WHEN
            component.onCountryPickerClick()

            // THEN
            assertEquals(SecondaryNavigationEvent.Open(AddressSecondaryContentEntry.COUNTRY_PICKER.key), awaitItem())
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
            component.show(AddressModel(country = "US", postalCode = "12345"))
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
    fun `when the form is shown with an address and confirmed, then that address is confirmed`() = runTest {
        // GIVEN
        val component = createComponent()
        val address = AddressModel(country = "NL", postalCode = "1234 AB")
        component.show(address)

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
    fun `when the form is shown without an address, then earlier values are dropped`() = runTest {
        // GIVEN
        val component = createComponent()
        component.show(AddressModel(country = "NL", postalCode = "1234 AB"))

        // WHEN
        component.show(address = null)

        // THEN
        component.eventFlow.test {
            component.confirm()
            expectNoEvents()
        }
    }

    /**
     * The form leaves composition whenever its screen closes, and is prefilled right before it opens again. Its text
     * fields report their first value back as input, so a view state that lags behind would undo the prefill.
     */
    @Test
    fun `when the form is shown again long after it closed, then it shows the address it is shown with`() =
        runTest {
            // GIVEN
            val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
            component.show(AddressModel(country = "NL", postalCode = "unconfirmed edit"))
            val subscription = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                component.viewState.collect {}
            }
            subscription.cancel()
            advanceTimeBy(NO_SUBSCRIBER_TIME_MS)

            // WHEN
            component.show(AddressModel(country = "NL", postalCode = "1234 AB"))

            // THEN
            val postalCode = component.viewState.value.elements
                .filterIsInstance<AddressFormElement.PostalCode>()
                .single()
            assertEquals("1234 AB", postalCode.textInputViewState.text)
        }

    @Test
    fun `when the component is created, then the country of the shopper locale is selected`() {
        // WHEN
        val component = createComponent()

        // THEN
        val country = component.viewState.value.elements
            .filterIsInstance<AddressFormElement.Country>()
            .single()
        assertEquals("US", country.selectedCountry?.isoCode)
    }

    @Test
    fun `when the component is created, then only the supported countries can be picked`() {
        // WHEN
        val component = createComponent()

        // THEN
        val countryCodes = component.viewState.value.countryPickerViewState.countries.map { it.isoCode }
        assertEquals(setOf("NL", "US"), countryCodes.toSet())
    }

    private fun createComponent(
        coroutineScope: CoroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
    ) = AddressComponentFactory().create(
        params = CheckoutParams(
            shopperLocale = Locale.forLanguageTag("en-US"),
            environment = Environment.TEST,
            clientKey = "test_client_key",
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
            amount = null,
            showSubmitButton = true,
            publicKey = null,
            additionalConfigurations = emptyMap(),
            additionalSessionParams = null,
        ),
        supportedCountryCodes = setOf("NL", "US"),
        coroutineScope = coroutineScope,
    )

    companion object {
        private val NETHERLANDS = CountryModel(isoCode = "NL", countryName = "Netherlands", callingCode = "+31")

        // Longer than any view state keeps its upstream alive without subscribers.
        private const val NO_SUBSCRIBER_TIME_MS = 60_000L
    }
}
