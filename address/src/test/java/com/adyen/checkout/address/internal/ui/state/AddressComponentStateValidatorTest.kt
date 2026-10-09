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

    private fun createState(address: AddressModel) = AddressComponentStateFactory(
        AddressComponentParams(shopperLocale = Locale.ROOT, supportedCountryCodes = emptySet()),
    ).createState(address)

    private fun AddressComponentState.withStateOptions(selected: String?) = copy(
        stateOrProvince = StateOrProvinceState.Options(
            regions = STATES,
            picker = PickerInputComponentState(selected = selected),
        ),
    )

    companion object {
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
    }
}
