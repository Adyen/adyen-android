/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 30/10/2026.
 */

package com.adyen.checkout.address.internal.ui

import com.adyen.checkout.address.internal.ui.model.AddressComponentParams
import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.address.internal.ui.state.AddressComponentStateFactory
import com.adyen.checkout.address.internal.ui.state.AddressComponentStatePostProcessor
import com.adyen.checkout.address.internal.ui.state.AddressComponentStateReducer
import com.adyen.checkout.address.internal.ui.state.AddressComponentStateValidator
import com.adyen.checkout.address.internal.ui.state.AddressFormElement
import com.adyen.checkout.address.internal.ui.state.AddressViewStateProducer
import com.adyen.checkout.address.internal.ui.view.AddressSecondaryContentEntry
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.test
import com.adyen.checkout.core.components.internal.ui.SecondaryNavigationEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
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
    fun `when shown then the form screen opens`() = runTest {
        // GIVEN
        val component = createComponent()
        val navigation = component.navigation.test(testScheduler)

        // WHEN
        component.show(address = null)

        // THEN
        assertEquals(listOf(SecondaryNavigationEvent.Open(AddressSecondaryContentEntry.FORM.key)), navigation.values)
    }

    @Test
    fun `when shown with an address then the form is prefilled`() = runTest {
        // GIVEN
        val component = createComponent()

        // WHEN
        component.show(US_ADDRESS)

        // THEN
        val elements = component.viewState.value.elements
        assertEquals("US", elements.element<AddressFormElement.Country>().selected?.code)
        assertEquals("1 Market Street", elements.element<AddressFormElement.Street>().textInputViewState.text)
        assertEquals("San Francisco", elements.element<AddressFormElement.City>().textInputViewState.text)
        assertEquals("CA", elements.element<AddressFormElement.StateOrProvince>().selected?.code)
        assertEquals("94105", elements.element<AddressFormElement.PostalCode>().textInputViewState.text)
    }

    @Test
    fun `when shown again then edits that were never confirmed are gone`() = runTest {
        // GIVEN
        val component = createComponent()
        component.show(US_ADDRESS)
        component.onCountrySelected("NL")

        // WHEN
        component.show(US_ADDRESS)

        // THEN
        val elements = component.viewState.value.elements
        assertEquals("US", elements.element<AddressFormElement.Country>().selected?.code)
        assertEquals("CA", elements.element<AddressFormElement.StateOrProvince>().selected?.code)
    }

    @Test
    fun `when the country row is clicked then the country picker screen opens`() = runTest {
        // GIVEN
        val component = createComponent()
        val navigation = component.navigation.test(testScheduler)

        // WHEN
        component.onCountryClick()

        // THEN
        assertEquals(
            listOf(SecondaryNavigationEvent.Open(AddressSecondaryContentEntry.COUNTRY_PICKER.key)),
            navigation.values,
        )
    }

    @Test
    fun `when the state row is clicked then the state picker screen opens`() = runTest {
        // GIVEN
        val component = createComponent()
        val navigation = component.navigation.test(testScheduler)

        // WHEN
        component.onStateOrProvinceClick()

        // THEN
        assertEquals(
            listOf(SecondaryNavigationEvent.Open(AddressSecondaryContentEntry.STATE_PICKER.key)),
            navigation.values,
        )
    }

    @Test
    fun `when a country is selected then the picker closes and the country is stored`() = runTest {
        // GIVEN
        val component = createComponent()
        component.show(address = null)
        val navigation = component.navigation.test(testScheduler)

        // WHEN
        component.onCountrySelected("CA")

        // THEN
        assertEquals(SecondaryNavigationEvent.Close, navigation.latestValue)
        assertEquals("CA", component.viewState.value.elements.element<AddressFormElement.Country>().selected?.code)
    }

    @Test
    fun `when a state is selected then the picker closes`() = runTest {
        // GIVEN
        val component = createComponent()
        component.show(address = null)
        val navigation = component.navigation.test(testScheduler)

        // WHEN
        component.onStateOrProvinceSelected("NY")

        // THEN
        assertEquals(SecondaryNavigationEvent.Close, navigation.latestValue)
    }

    @Test
    fun `when a valid form is confirmed then the address is emitted and the form closes`() = runTest {
        // GIVEN
        val component = createComponent()
        component.show(US_ADDRESS)
        val events = component.eventFlow.test(testScheduler)
        val navigation = component.navigation.test(testScheduler)

        // WHEN
        component.confirm()

        // THEN
        assertEquals(listOf(AddressComponentEvent.Confirmed(US_ADDRESS)), events.values)
        assertEquals(SecondaryNavigationEvent.Close, navigation.latestValue)
    }

    @Test
    fun `when the form is confirmed then the text fields are trimmed and blank ones are left out`() = runTest {
        // GIVEN
        val component = createComponent()
        component.show(
            US_ADDRESS.copy(
                street = " 1 Market Street ",
                houseNumberOrName = "  ",
                postalCode = "94105 ",
                city = " San Francisco",
            ),
        )
        val events = component.eventFlow.test(testScheduler)

        // WHEN
        component.confirm()

        // THEN
        assertEquals(listOf(AddressComponentEvent.Confirmed(US_ADDRESS)), events.values)
    }

    @Test
    fun `when an invalid form is confirmed then the errors are shown and the form stays open`() = runTest {
        // GIVEN
        val component = createComponent()
        component.show(address = null)
        val events = component.eventFlow.test(testScheduler)
        val navigation = component.navigation.test(testScheduler)

        // WHEN
        component.confirm()

        // THEN
        val street = component.viewState.value.elements.element<AddressFormElement.Street>()
        assertEquals(CheckoutLocalizationKey.ADDRESS_STREET_ERROR, street.textInputViewState.supportingText)
        assertTrue(events.values.isEmpty())
        assertFalse(SecondaryNavigationEvent.Close in navigation.values)
    }

    @Test
    fun `when asked about one of its own screens then the component has it`() = runTest {
        // GIVEN
        val component = createComponent()

        // THEN
        AddressSecondaryContentEntry.entries.forEach { entry ->
            assertTrue(component.hasScreen(entry.key))
        }
    }

    @Test
    fun `when asked about another component's screen then the component does not have it`() = runTest {
        // GIVEN
        val component = createComponent()

        // THEN
        assertFalse(component.hasScreen("INSTALLMENTS"))
    }

    /** The form's text fields report their first value back as input, so a lagging view state would undo the reset. */
    @Test
    fun `when the form is closed and reopened after the sharing timeout then the prefill is not overwritten`() =
        runTest {
            // GIVEN
            val component = createComponent()
            component.show(US_ADDRESS.copy(postalCode = "unconfirmed edit"))
            val subscription = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                component.viewState.collect {}
            }
            subscription.cancel()
            advanceTimeBy(NO_SUBSCRIBER_TIME_MS)

            // WHEN
            component.show(US_ADDRESS)

            // THEN
            val postalCode = component.viewState.value.elements.element<AddressFormElement.PostalCode>()
            assertEquals("94105", postalCode.textInputViewState.text)
        }

    private fun TestScope.createComponent(): AddressComponent {
        val componentParams = AddressComponentParams(
            shopperLocale = SHOPPER_LOCALE,
            supportedCountryCodes = emptySet(),
        )
        val componentStateFactory = AddressComponentStateFactory(componentParams)
        return AddressComponent(
            componentStateValidator = AddressComponentStateValidator(),
            componentStateFactory = componentStateFactory,
            componentStateReducer = AddressComponentStateReducer(componentParams, componentStateFactory),
            componentStatePostProcessor = AddressComponentStatePostProcessor(),
            viewStateProducer = AddressViewStateProducer(SHOPPER_LOCALE),
            coroutineScope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)),
        )
    }

    private inline fun <reified T : AddressFormElement> List<AddressFormElement>.element(): T =
        filterIsInstance<T>().single()

    companion object {
        private val SHOPPER_LOCALE = Locale.US

        private val US_ADDRESS = AddressModel(
            country = "US",
            street = "1 Market Street",
            houseNumberOrName = null,
            postalCode = "94105",
            city = "San Francisco",
            stateOrProvince = "CA",
        )

        // Longer than any view state keeps its upstream alive without subscribers.
        private const val NO_SUBSCRIBER_TIME_MS = 60_000L
    }
}
