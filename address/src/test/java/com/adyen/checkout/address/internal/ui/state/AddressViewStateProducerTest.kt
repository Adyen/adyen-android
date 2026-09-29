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
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.form.KeyboardAction
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class AddressViewStateProducerTest {

    private val producer = AddressViewStateProducer()

    @Test
    fun `when produce is called, then the form and the country picker are created`() {
        val state = createState(
            country = CountryInputState(selectedCountry = NETHERLANDS),
            postalCode = TextInputComponentState(
                text = "1",
                error = TextInputComponentState.InputError(CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_ERROR, true),
            ),
        ).copy(focusRequest = FocusRequest(AddressFormElementId.POSTAL_CODE))

        val actual = producer.produce(state)

        val expected = AddressViewState(
            elements = listOf(
                AddressFormElement.Country(selectedCountry = NETHERLANDS, errorMessage = null),
                AddressFormElement.PostalCode(
                    textInputViewState = TextInputViewState(
                        text = "1",
                        supportingText = CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_ERROR,
                        isError = true,
                        // The only text input of the form, so it closes the keyboard rather than moving on.
                        keyboardAction = KeyboardAction.DONE,
                        isFocusRequested = true,
                    ),
                ),
            ),
            countryPickerViewState = CountryPickerViewState(
                countries = listOf(NETHERLANDS, UNITED_STATES),
                selectedCountry = NETHERLANDS,
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `when the country error is shown, then the country element carries it`() {
        val state = createState(
            country = CountryInputState(
                selectedCountry = null,
                error = TextInputComponentState.InputError(CheckoutLocalizationKey.ADDRESS_COUNTRY_ERROR, true),
            ),
        )

        val actual = producer.produce(state).elements.first()

        assertEquals(
            AddressFormElement.Country(
                selectedCountry = null,
                errorMessage = CheckoutLocalizationKey.ADDRESS_COUNTRY_ERROR,
            ),
            actual,
        )
    }

    @Test
    fun `when the country error is held back, then the country element does not carry it`() {
        val state = createState(
            country = CountryInputState(
                selectedCountry = null,
                error = TextInputComponentState.InputError(CheckoutLocalizationKey.ADDRESS_COUNTRY_ERROR, false),
            ),
        )

        val actual = producer.produce(state).elements.first() as AddressFormElement.Country

        assertNull(actual.errorMessage)
    }

    @Test
    fun `when produce is called, then the elements follow the form order`() {
        val state = createState()

        val actual = producer.produce(state).elements.map { it.id }

        assertEquals(listOf(AddressFormElementId.COUNTRY, AddressFormElementId.POSTAL_CODE), actual)
    }

    private fun createState(
        country: CountryInputState = CountryInputState(selectedCountry = NETHERLANDS),
        postalCode: TextInputComponentState = TextInputComponentState(),
    ) = AddressComponentState(
        countries = listOf(NETHERLANDS, UNITED_STATES),
        country = country,
        postalCode = postalCode,
    )

    companion object {
        private val NETHERLANDS = CountryModel(isoCode = "NL", countryName = "Netherlands", callingCode = "+31")
        private val UNITED_STATES = CountryModel(isoCode = "US", countryName = "United States", callingCode = "+1")
    }
}
