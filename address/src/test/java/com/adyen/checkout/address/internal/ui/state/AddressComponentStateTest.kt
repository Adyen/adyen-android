/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.form.FormElementState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AddressComponentStateTest {

    @Test
    fun `when the form is derived, then the country comes before the postal code`() {
        val state = createState()

        val actual = state.form.elements.map { it.id }

        assertEquals(listOf(AddressFormElementId.COUNTRY, AddressFormElementId.POSTAL_CODE), actual)
    }

    @Test
    fun `when the country and the postal code have errors, then both elements are invalid`() {
        val state = createState(
            country = CountryInputState(error = TextInputComponentState.InputError(ANY_ERROR)),
            postalCode = TextInputComponentState(error = TextInputComponentState.InputError(ANY_ERROR)),
        )

        val actual = state.form.elements

        val expected = listOf(
            FormElementState(AddressFormElementId.COUNTRY, isValid = false),
            FormElementState(AddressFormElementId.POSTAL_CODE, isValid = false),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `when the country and the postal code have no errors, then the form is valid`() {
        val state = createState()

        val actual = state.form.isFormValid

        assertEquals(true, actual)
    }

    @Test
    fun `when the state is converted to an address, then the selected country and the postal code are used`() {
        val state = createState(
            country = CountryInputState(selectedCountry = NETHERLANDS),
            postalCode = TextInputComponentState(text = "1234 AB"),
        )

        val actual = state.toAddressModel()

        assertEquals(AddressModel(country = "NL", postalCode = "1234 AB"), actual)
    }

    @Test
    fun `when nothing is entered, then the address has no values`() {
        val state = createState(postalCode = TextInputComponentState(text = "  "))

        val actual = state.toAddressModel()

        assertEquals(AddressModel(country = null, postalCode = null), actual)
    }

    private fun createState(
        country: CountryInputState = CountryInputState(),
        postalCode: TextInputComponentState = TextInputComponentState(),
    ) = AddressComponentState(
        countries = listOf(NETHERLANDS),
        country = country,
        postalCode = postalCode,
    )

    companion object {
        private val NETHERLANDS = CountryModel(isoCode = "NL", countryName = "Netherlands", callingCode = "+31")
        private val ANY_ERROR = CheckoutLocalizationKey.GENERAL_CLOSE
    }
}
