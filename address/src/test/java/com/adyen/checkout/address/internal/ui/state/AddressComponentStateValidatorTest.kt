/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class AddressComponentStateValidatorTest {

    private val validator = AddressComponentStateValidator()

    @Test
    fun `when no country is selected, then the country has an error`() {
        val state = createState(country = CountryInputState(selectedCountry = null))

        val actual = validator.validate(state)

        assertEquals(CheckoutLocalizationKey.ADDRESS_COUNTRY_ERROR, actual.country.error?.message)
    }

    @Test
    fun `when a country is selected, then the country has no error`() {
        val state = createState(
            country = CountryInputState(
                selectedCountry = NETHERLANDS,
                error = TextInputComponentState.InputError(CheckoutLocalizationKey.ADDRESS_COUNTRY_ERROR),
            ),
        )

        val actual = validator.validate(state)

        assertNull(actual.country.error)
    }

    @Test
    fun `when the country error is already shown, then validating again keeps it shown`() {
        val state = createState(
            country = CountryInputState(
                selectedCountry = null,
                error = TextInputComponentState.InputError(CheckoutLocalizationKey.ADDRESS_COUNTRY_ERROR, true),
            ),
        )

        val actual = validator.validate(state)

        assertEquals(true, actual.country.error?.isVisible)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "12345678901"])
    fun `when the postal code is blank or too long, then it has an error`(postalCode: String) {
        val state = createState(postalCode = TextInputComponentState(text = postalCode))

        val actual = validator.validate(state)

        assertEquals(CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_ERROR, actual.postalCode.error?.message)
    }

    @ParameterizedTest
    @ValueSource(strings = ["1", "1234 AB", "1234567890"])
    fun `when the postal code is filled in, then it has no error`(postalCode: String) {
        val state = createState(postalCode = TextInputComponentState(text = postalCode))

        val actual = validator.validate(state)

        assertNull(actual.postalCode.error)
    }

    @Test
    fun `when every field is valid, then the state is valid`() {
        val state = validator.validate(createState())

        assertTrue(validator.isValid(state))
    }

    @Test
    fun `when a field is invalid, then the state is invalid`() {
        val state = validator.validate(createState(country = CountryInputState(selectedCountry = null)))

        assertFalse(validator.isValid(state))
    }

    private fun createState(
        country: CountryInputState = CountryInputState(selectedCountry = NETHERLANDS),
        postalCode: TextInputComponentState = TextInputComponentState(text = "1234 AB"),
    ) = AddressComponentState(
        countries = listOf(NETHERLANDS),
        country = country,
        postalCode = postalCode,
    )

    companion object {
        private val NETHERLANDS = CountryModel(isoCode = "NL", countryName = "Netherlands", callingCode = "+31")
    }
}
