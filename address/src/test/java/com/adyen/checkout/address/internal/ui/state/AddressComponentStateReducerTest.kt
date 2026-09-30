/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressComponentParams
import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class AddressComponentStateReducerTest {

    private val componentStateFactory = AddressComponentStateFactory(
        componentParams = AddressComponentParams(
            shopperLocale = Locale.forLanguageTag("en-US"),
            supportedCountryCodes = setOf("NL", "US"),
        ),
    )

    private val reducer = AddressComponentStateReducer(componentStateFactory)

    @Test
    fun `when intent is UpdateCountry, then the country is selected`() {
        val state = componentStateFactory.createInitialState()

        val actual = reducer.reduce(state, AddressIntent.UpdateCountry(NETHERLANDS))

        val expected = state.copy(country = state.country.copy(selectedCountry = NETHERLANDS))
        assertEquals(expected, actual)
    }

    @Test
    fun `when intent is UpdatePostalCode, then the postal code is updated`() {
        val state = componentStateFactory.createInitialState()

        val actual = reducer.reduce(state, AddressIntent.UpdatePostalCode("1234 AB"))

        val expected = state.copy(postalCode = state.postalCode.copy(text = "1234 AB"))
        assertEquals(expected, actual)
    }

    @Test
    fun `when intent is UpdatePostalCode and an error is shown, then the error is hidden while typing`() {
        val error = TextInputComponentState.InputError(CheckoutLocalizationKey.GENERAL_CLOSE, isVisible = true)
        val state = componentStateFactory.createInitialState().copy(
            postalCode = TextInputComponentState(error = error),
        )

        val actual = reducer.reduce(state, AddressIntent.UpdatePostalCode("1"))

        assertEquals(false, actual.postalCode.error?.isVisible)
    }

    @Test
    fun `when intent is Prefill with an address, then the form is replaced by the address`() {
        val state = componentStateFactory.createInitialState().copy(
            postalCode = TextInputComponentState(text = "unconfirmed edit"),
        )
        val address = AddressModel(country = "NL", postalCode = "1234 AB")

        val actual = reducer.reduce(state, AddressIntent.Prefill(address))

        assertEquals(componentStateFactory.createState(address), actual)
    }

    @Test
    fun `when intent is Prefill without an address, then the form is reset to its initial state`() {
        val state = componentStateFactory.createInitialState().copy(
            country = CountryInputState(selectedCountry = NETHERLANDS),
            postalCode = TextInputComponentState(text = "unconfirmed edit"),
        )

        val actual = reducer.reduce(state, AddressIntent.Prefill(address = null))

        assertEquals(componentStateFactory.createInitialState(), actual)
    }

    @ParameterizedTest
    @MethodSource("intentsWithoutValueChanges")
    fun `when intent does not change a value, then the state is untouched`(intent: AddressIntent) {
        val state = componentStateFactory.createInitialState()

        val actual = reducer.reduce(state, intent)

        assertEquals(state, actual)
    }

    companion object {
        private val NETHERLANDS = CountryModel(isoCode = "NL", countryName = "Netherlands", callingCode = "+31")

        @JvmStatic
        fun intentsWithoutValueChanges() = listOf(
            Arguments.of(AddressIntent.UpdateFieldFocus(AddressFormElementId.POSTAL_CODE, hasFocus = true)),
            Arguments.of(AddressIntent.FocusRequestConsumed(AddressFormElementId.POSTAL_CODE)),
            Arguments.of(AddressIntent.HighlightValidationErrors),
        )
    }
}
