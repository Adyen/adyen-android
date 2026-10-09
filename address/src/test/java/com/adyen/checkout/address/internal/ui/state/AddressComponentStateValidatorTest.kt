/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 9/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressComponentParams
import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.address.internal.ui.model.AddressRegion
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState.InputError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class AddressComponentStateValidatorTest {

    private val validator = AddressComponentStateValidator()

    @Test
    fun `when a required field is blank then it has an error`() {
        // GIVEN
        val state = createState(NL_ADDRESS.copy(street = " ", houseNumberOrName = null, postalCode = null, city = null))

        // WHEN
        val result = validator.validate(state)

        // THEN
        assertEquals(CheckoutLocalizationKey.ADDRESS_STREET_ERROR, result.street.error?.message)
        assertEquals(CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_ERROR, result.houseNumberOrName.error?.message)
        assertEquals(CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_ERROR, result.postalCode.error?.message)
        assertEquals(CheckoutLocalizationKey.ADDRESS_CITY_ERROR, result.city.error?.message)
    }

    @Test
    fun `when an optional field is blank then it has no error`() {
        // GIVEN
        val state = createState(US_ADDRESS.copy(houseNumberOrName = null))

        // WHEN
        val result = validator.validate(state)

        // THEN
        assertNull(result.houseNumberOrName.error)
    }

    @Test
    fun `when no country is selected then the country has an error`() {
        // GIVEN
        val state = createState(NL_ADDRESS.copy(country = null))

        // WHEN
        val result = validator.validate(state)

        // THEN
        assertEquals(CheckoutLocalizationKey.ADDRESS_COUNTRY_ERROR, result.country.error?.message)
    }

    @Test
    fun `when the state has options and none is selected then the state has an error`() {
        // GIVEN
        val state = createState(US_ADDRESS).withStateOptions(selected = null)

        // WHEN
        val result = validator.validate(state)

        // THEN
        val options = result.stateOrProvince as StateOrProvinceState.Options
        assertEquals(CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_ERROR, options.picker.error?.message)
    }

    @Test
    fun `when the free text state is blank then it has the free text error`() {
        // GIVEN
        val state = createState(US_ADDRESS).copy(
            stateOrProvince = StateOrProvinceState.FreeText(input = TextInputComponentState(text = " ")),
        )

        // WHEN
        val result = validator.validate(state)

        // THEN
        val freeText = result.stateOrProvince as StateOrProvinceState.FreeText
        assertEquals(CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_ERROR_FREE_TEXT, freeText.input.error?.message)
    }

    @Test
    fun `when the state is unavailable then it does not affect the form validity`() {
        // GIVEN
        val state = createState(NL_ADDRESS).copy(stateOrProvince = StateOrProvinceState.Unavailable)

        // WHEN
        val isValid = validator.isValid(validator.validate(state))

        // THEN
        assertTrue(isValid)
    }

    @Test
    fun `when the country list failed to load but a country is selected then the country is valid`() {
        // GIVEN
        val state = createState(NL_ADDRESS).copy(countries = emptyList(), isLoadingCountries = false)

        // WHEN
        val result = validator.validate(state)

        // THEN
        assertNull(result.country.error)
    }

    @Test
    fun `when every visible field is filled then the form is valid`() {
        // GIVEN
        val state = createState(US_ADDRESS).withStateOptions(selected = "CA")

        // WHEN
        val isValid = validator.isValid(validator.validate(state))

        // THEN
        assertTrue(isValid)
    }

    @Test
    fun `when the country is CA and the house number is blank then the form is valid`() {
        // GIVEN
        val state = createState(CA_ADDRESS.copy(houseNumberOrName = null)).withStateOptions(selected = "ON")

        // WHEN
        val isValid = validator.isValid(validator.validate(state))

        // THEN
        assertTrue(isValid)
    }

    @Test
    fun `when the country is BR and the house number is blank then the form is invalid`() {
        // GIVEN
        val state = createState(BR_ADDRESS.copy(houseNumberOrName = null)).withStateOptions(selected = "SP")

        // WHEN
        val isValid = validator.isValid(validator.validate(state))

        // THEN
        assertFalse(isValid)
    }

    @Test
    fun `when an error is already visible then revalidating keeps it visible`() {
        // GIVEN
        val visibleError = InputError(CheckoutLocalizationKey.ADDRESS_STREET_ERROR, isVisible = true)
        val state = createState(US_ADDRESS).copy(
            street = TextInputComponentState(error = visibleError),
            stateOrProvince = StateOrProvinceState.Options(
                regions = STATES,
                picker = PickerInputComponentState(error = visibleError),
            ),
        )

        // WHEN
        val result = validator.validate(state)

        // THEN
        val options = result.stateOrProvince as StateOrProvinceState.Options
        assertTrue(result.street.error?.isVisible == true)
        assertTrue(options.picker.error?.isVisible == true)
    }

    @ParameterizedTest
    @MethodSource("matchingPostalCodeSource")
    fun `when the postal code matches the country pattern then it is valid`(country: String, postalCode: String) {
        // GIVEN
        val state = createState(NL_ADDRESS.copy(country = country, postalCode = postalCode))

        // WHEN
        val result = validator.validate(state)

        // THEN
        assertNull(result.postalCode.error)
    }

    /**
     * The regression test for matching unanchored, like Web: anchoring the patterns would reject these.
     */
    @ParameterizedTest
    @MethodSource("documentedVariantPostalCodeSource")
    fun `when the postal code is a documented variant then it is valid`(country: String, postalCode: String) {
        // GIVEN
        val state = createState(NL_ADDRESS.copy(country = country, postalCode = postalCode))

        // WHEN
        val result = validator.validate(state)

        // THEN
        assertNull(result.postalCode.error)
    }

    @ParameterizedTest
    @MethodSource("mismatchingPostalCodeSource")
    fun `when the postal code does not match the country pattern then it is invalid`(
        country: String,
        postalCode: String,
    ) {
        // GIVEN
        val state = createState(NL_ADDRESS.copy(country = country, postalCode = postalCode))

        // WHEN
        val result = validator.validate(state)

        // THEN
        assertEquals(CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_ERROR, result.postalCode.error?.message)
    }

    @Test
    fun `when the country has no pattern then any non blank postal code is valid`() {
        // GIVEN
        val state = createState(NL_ADDRESS.copy(country = "AR", postalCode = "C1425 abc"))

        // WHEN
        val result = validator.validate(state)

        // THEN
        assertNull(result.postalCode.error)
    }

    @Test
    fun `when the postal code is blank then it is invalid`() {
        // GIVEN
        val state = createState(NL_ADDRESS.copy(country = "AR", postalCode = " "))

        // WHEN
        val result = validator.validate(state)

        // THEN
        assertEquals(CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_ERROR, result.postalCode.error?.message)
    }

    @ParameterizedTest
    @MethodSource("lowerCasePostalCodeSource")
    fun `when the postal code is lower case then it is still valid`(country: String, postalCode: String) {
        // GIVEN
        val state = createState(NL_ADDRESS.copy(country = country, postalCode = postalCode))

        // WHEN
        val result = validator.validate(state)

        // THEN
        assertNull(result.postalCode.error)
    }

    @Test
    fun `when the country changes then the postal code is revalidated`() {
        // GIVEN
        val state = validator.validate(createState(NL_ADDRESS))
        val reducer = AddressComponentStateReducer(PARAMS, AddressComponentStateFactory(PARAMS))

        // WHEN
        val result = validator.validate(reducer.reduce(state, AddressIntent.UpdateCountry("US")))

        // THEN
        assertNull(state.postalCode.error)
        assertEquals(CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_ERROR, result.postalCode.error?.message)
    }

    @Test
    fun `when the street has invalid characters then it has an invalid characters error`() {
        // GIVEN
        val state = createState(NL_ADDRESS.copy(street = "Simon Carmiggeltstraat 🏠"))

        // WHEN
        val result = validator.validate(state)

        // THEN
        assertEquals(CheckoutLocalizationKey.ADDRESS_INVALID_CHARACTERS_ERROR, result.street.error?.message)
    }

    @Test
    fun `when the house number has invalid characters then it has an invalid characters error`() {
        // GIVEN
        val state = createState(NL_ADDRESS.copy(houseNumberOrName = "6\u0000"))

        // WHEN
        val result = validator.validate(state)

        // THEN
        assertEquals(CheckoutLocalizationKey.ADDRESS_INVALID_CHARACTERS_ERROR, result.houseNumberOrName.error?.message)
    }

    @Test
    fun `when the city has invalid characters then it has an invalid characters error`() {
        // GIVEN
        val state = createState(NL_ADDRESS.copy(city = "Amsterdam 🇳🇱"))

        // WHEN
        val result = validator.validate(state)

        // THEN
        assertEquals(CheckoutLocalizationKey.ADDRESS_INVALID_CHARACTERS_ERROR, result.city.error?.message)
    }

    @Test
    fun `when an optional field has invalid characters then it has an invalid characters error`() {
        // GIVEN
        val state = createState(US_ADDRESS.copy(houseNumberOrName = "Apt 4 😀"))

        // WHEN
        val result = validator.validate(state)

        // THEN
        assertEquals(CheckoutLocalizationKey.ADDRESS_INVALID_CHARACTERS_ERROR, result.houseNumberOrName.error?.message)
    }

    @Test
    fun `when the postal code has invalid characters then it has no invalid characters error`() {
        // GIVEN
        val state = createState(NL_ADDRESS.copy(country = "AR", postalCode = "C1425 😀"))

        // WHEN
        val result = validator.validate(state)

        // THEN
        assertNull(result.postalCode.error)
    }

    private fun createState(address: AddressModel) = AddressComponentStateFactory(PARAMS).createState(address)

    private fun AddressComponentState.withStateOptions(selected: String?) = copy(
        stateOrProvince = StateOrProvinceState.Options(
            regions = STATES,
            picker = PickerInputComponentState(selected = selected),
        ),
    )

    companion object {
        private val PARAMS = AddressComponentParams(shopperLocale = Locale.ROOT, supportedCountryCodes = emptySet())

        private val NL_ADDRESS = AddressModel(
            country = "NL",
            street = "Simon Carmiggeltstraat",
            houseNumberOrName = "6",
            postalCode = "1011 DJ",
            city = "Amsterdam",
            stateOrProvince = null,
        )

        private val US_ADDRESS = AddressModel(
            country = "US",
            street = "Main Street 1",
            houseNumberOrName = "Apt 4",
            postalCode = "94107",
            city = "San Francisco",
            stateOrProvince = null,
        )

        private val CA_ADDRESS = AddressModel(
            country = "CA",
            street = "Wellington Street 111",
            houseNumberOrName = "Suite 2",
            postalCode = "K1A 0A9",
            city = "Ottawa",
            stateOrProvince = null,
        )

        private val BR_ADDRESS = AddressModel(
            country = "BR",
            street = "Avenida Paulista",
            houseNumberOrName = "1000",
            postalCode = "01310-100",
            city = "São Paulo",
            stateOrProvince = null,
        )

        private val STATES = listOf(
            AddressRegion(code = "CA", name = "California"),
            AddressRegion(code = "ON", name = "Ontario"),
            AddressRegion(code = "SP", name = "São Paulo"),
        )

        @JvmStatic
        fun matchingPostalCodeSource() = listOf(
            arguments("NL", "1234AB"),
            arguments("NL", "1234 AB"),
            arguments("US", "12345"),
            arguments("US", "94107-1234"),
            arguments("JP", "107-0052"),
            arguments("JP", "1070052"),
            arguments("GB", "SW1A 1AA"),
            arguments("GB", "GIR 0AA"),
            arguments("CA", "K1A 0B1"),
            arguments("BR", "12345-678"),
        )

        @JvmStatic
        fun documentedVariantPostalCodeSource() = listOf(
            arguments("SI", "SI-1234"),
            arguments("SK", "SK-12345"),
        )

        @JvmStatic
        fun mismatchingPostalCodeSource() = listOf(
            arguments("US", "1234"),
            arguments("US", "12345abc"),
            arguments("US", "12345-12"),
            arguments("JP", "107-005"),
            arguments("NL", "1234"),
            arguments("CA", "12345"),
            arguments("BR", "1234567"),
        )

        @JvmStatic
        fun lowerCasePostalCodeSource() = listOf(
            arguments("NL", "1234ab"),
            arguments("CA", "k1a 0b1"),
        )
    }
}
