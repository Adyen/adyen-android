/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 30/10/2026.
 */

package com.adyen.checkout.address.internal.ui

import com.adyen.checkout.address.internal.data.api.AddressRepository
import com.adyen.checkout.address.internal.data.model.AddressItem
import com.adyen.checkout.address.internal.ui.model.AddressComponentParams
import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.address.internal.ui.model.AddressRegion
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
internal class AddressComponentTest {

    private lateinit var addressRepository: TestAddressRepository

    @BeforeEach
    fun beforeEach() {
        addressRepository = TestAddressRepository()
    }

    @Test
    fun `when created then the countries are loaded and no states are requested`() = runTest {
        // WHEN
        val component = createComponent()

        // THEN
        assertEquals(COUNTRY_REGIONS, component.viewState.value.countryPickerViewState.regions)
        assertTrue(addressRepository.requestedStates.isEmpty())
    }

    @Test
    fun `when the countries request fails then the prefilled country is kept`() = runTest {
        // GIVEN
        addressRepository.countriesResult = Result.failure(IOException("offline"))
        val component = createComponent()

        // WHEN
        component.show(US_ADDRESS)

        // THEN
        assertTrue(component.viewState.value.countryPickerViewState.regions.isEmpty())
        assertEquals("US", component.viewState.value.elements.element<AddressFormElement.Country>().selected?.code)
    }

    @Test
    fun `when shown after the countries request failed then the countries are requested again`() = runTest {
        // GIVEN
        addressRepository.countriesResult = Result.failure(IOException("offline"))
        val component = createComponent()
        addressRepository.countriesResult = Result.success(COUNTRIES)

        // WHEN
        component.show(address = null)

        // THEN
        assertEquals(2, addressRepository.countriesRequests)
        assertEquals(COUNTRY_REGIONS, component.viewState.value.countryPickerViewState.regions)
    }

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
    fun `when shown then the states of the selected country are requested`() = runTest {
        // GIVEN
        val component = createComponent()

        // WHEN
        component.show(US_ADDRESS)

        // THEN
        assertEquals(listOf("US"), addressRepository.requestedStates)
        assertEquals(US_STATE_REGIONS, component.viewState.value.statePickerViewState?.regions)
    }

    @Test
    fun `when shown with a country without states then no states are requested`() = runTest {
        // GIVEN
        val component = createComponent()

        // WHEN
        component.show(US_ADDRESS.copy(country = "NL", stateOrProvince = null))

        // THEN
        assertTrue(addressRepository.requestedStates.isEmpty())
    }

    @Test
    fun `when shown with no country selected then no states are requested`() = runTest {
        // GIVEN
        // The shopper locale country, US, cannot be picked, so nothing is preselected.
        val component = createComponent(supportedCountryCodes = setOf("NL", "GB"))

        // WHEN
        component.show(address = null)

        // THEN
        assertNull(component.viewState.value.elements.element<AddressFormElement.Country>().selected)
        assertTrue(addressRepository.requestedStates.isEmpty())
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
    fun `when a country is selected then its states are requested`() = runTest {
        // GIVEN
        val component = createComponent()
        component.show(address = null)

        // WHEN
        component.onCountrySelected("CA")

        // THEN
        assertEquals(listOf("US", "CA"), addressRepository.requestedStates)
        assertEquals(CA_STATE_REGIONS, component.viewState.value.statePickerViewState?.regions)
    }

    @Test
    fun `when a country without states is selected then no states are requested for it`() = runTest {
        // GIVEN
        val component = createComponent()
        component.show(address = null)

        // WHEN
        component.onCountrySelected("NL")

        // THEN
        assertEquals(listOf("US"), addressRepository.requestedStates)
        assertNull(component.viewState.value.statePickerViewState)
    }

    @Test
    fun `when the country is changed while its states are loading then the previous request is cancelled`() =
        runTest {
            // GIVEN
            val component = createComponent()
            component.show(address = null)
            val statesAnswer = CompletableDeferred<Unit>()
            addressRepository.statesAnswer = statesAnswer
            component.onCountrySelected("CA")

            // WHEN
            component.onCountrySelected("NL")

            // THEN
            assertEquals(listOf("CA"), addressRepository.cancelledStates)
        }

    @Test
    fun `when shown with another country while the states are loading then the previous request is cancelled`() =
        runTest {
            // GIVEN
            val component = createComponent()
            addressRepository.statesAnswer = CompletableDeferred()
            component.show(US_ADDRESS)

            // WHEN
            component.show(US_ADDRESS.copy(country = "CA", stateOrProvince = null))

            // THEN
            assertEquals(listOf("US"), addressRepository.cancelledStates)
            assertEquals(listOf("US", "CA"), addressRepository.requestedStates)
        }

    @Test
    fun `when shown again after the states request failed then they are requested again`() = runTest {
        // GIVEN
        addressRepository.statesResults["US"] = Result.failure(IOException("offline"))
        val component = createComponent()
        component.show(US_ADDRESS)
        addressRepository.statesResults["US"] = Result.success(US_STATES)

        // WHEN
        component.show(US_ADDRESS)

        // THEN
        assertEquals(listOf("US", "US"), addressRepository.requestedStates)
        assertEquals(US_STATE_REGIONS, component.viewState.value.statePickerViewState?.regions)
    }

    @Test
    fun `when a state is selected then the picker closes and the state is stored`() = runTest {
        // GIVEN
        val component = createComponent()
        component.show(address = null)
        val navigation = component.navigation.test(testScheduler)

        // WHEN
        component.onStateOrProvinceSelected("NY")

        // THEN
        assertEquals(SecondaryNavigationEvent.Close, navigation.latestValue)
        val state = component.viewState.value.elements.element<AddressFormElement.StateOrProvince>()
        assertEquals(AddressRegion(code = "NY", name = "New York"), state.selected)
    }

    @Test
    fun `when the state request fails then the state field stays empty and saving shows its error`() = runTest {
        // GIVEN
        addressRepository.statesResults["US"] = Result.failure(IOException("offline"))
        val component = createComponent()
        component.show(US_ADDRESS.copy(stateOrProvince = null))
        val events = component.eventFlow.test(testScheduler)

        // WHEN
        component.confirm()

        // THEN
        val state = component.viewState.value.elements.element<AddressFormElement.StateOrProvince>()
        assertNull(state.selected)
        assertEquals(CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_ERROR, state.errorMessage)
        assertTrue(events.values.isEmpty())
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

    private fun TestScope.createComponent(
        supportedCountryCodes: Set<String> = emptySet(),
    ): AddressComponent {
        val componentParams = AddressComponentParams(
            shopperLocale = SHOPPER_LOCALE,
            supportedCountryCodes = supportedCountryCodes,
        )
        val componentStateFactory = AddressComponentStateFactory(componentParams)
        return AddressComponent(
            addressRepository = addressRepository,
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

    private class TestAddressRepository : AddressRepository {

        var countriesResult = Result.success(COUNTRIES)

        var countriesRequests = 0
            private set

        val statesResults = mutableMapOf(
            "US" to Result.success(US_STATES),
            "CA" to Result.success(CA_STATES),
        )

        /** When set, every states request waits for it to complete. */
        var statesAnswer: CompletableDeferred<Unit>? = null

        val requestedStates = mutableListOf<String>()
        val cancelledStates = mutableListOf<String>()

        override suspend fun getCountries(): Result<List<AddressItem>> {
            countriesRequests++
            return countriesResult
        }

        override suspend fun getStates(countryCode: String): Result<List<AddressItem>> {
            requestedStates += countryCode
            try {
                statesAnswer?.await()
            } catch (e: CancellationException) {
                cancelledStates += countryCode
                throw e
            }
            return statesResults[countryCode] ?: Result.success(emptyList())
        }
    }

    companion object {
        private val SHOPPER_LOCALE = Locale.US

        private val COUNTRIES = listOf(
            AddressItem(id = "CA", name = "Canada"),
            AddressItem(id = "GB", name = "United Kingdom"),
            AddressItem(id = "NL", name = "Netherlands"),
            AddressItem(id = "US", name = "United States"),
        )
        private val COUNTRY_REGIONS = COUNTRIES.map { AddressRegion(code = it.id!!, name = it.name!!) }

        private val US_STATES = listOf(
            AddressItem(id = "CA", name = "California"),
            AddressItem(id = "NY", name = "New York"),
        )
        private val US_STATE_REGIONS = US_STATES.map { AddressRegion(code = it.id!!, name = it.name!!) }

        private val CA_STATES = listOf(AddressItem(id = "ON", name = "Ontario"))
        private val CA_STATE_REGIONS = CA_STATES.map { AddressRegion(code = it.id!!, name = it.name!!) }

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
