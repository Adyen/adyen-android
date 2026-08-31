package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

internal class MBWayComponentStateReducerTest {

    private lateinit var reducer: MBWayComponentStateReducer

    @BeforeEach
    fun beforeEach() {
        reducer = MBWayComponentStateReducer()
    }

    @Test
    fun `when intent is UpdateCountry, then state is updated`() {
        val country = CountryModel("PT", "351", "Portugal")
        val state = createInitialState()

        val actual = reducer.reduce(state, MBWayIntent.UpdateCountry(country))

        val expected = state.copy(selectedCountryCode = country)
        assertEquals(expected, actual)
    }

    @Test
    fun `when intent is UpdateLoading, then state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, MBWayIntent.UpdateLoading(true))

        val expected = state.copy(isLoading = true)
        assertEquals(expected, actual)
    }

    @Test
    fun `when intent is UpdatePhoneNumber, then state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, MBWayIntent.UpdatePhoneNumber("123"))

        val expected = state.copy(phoneNumber = state.phoneNumber.copy(text = "123"))
        assertEquals(expected, actual)
    }

    @Test
    fun `when the phone number loses focus, then an error it was holding back is shown`() {
        val state = createInitialState().copy(
            phoneNumber = TextInputComponentState(
                error = TextInputComponentState.InputError(CheckoutLocalizationKey.GENERAL_CLOSE),
            ),
        )

        val actual = reducer.reduce(state, MBWayIntent.UpdateFieldFocus(MBWayFieldId.PHONE_NUMBER, hasFocus = false))

        assertTrue(actual.phoneNumber.isErrorVisible)
    }

    @Test
    fun `when the shopper focuses the phone number, then a visible error is hidden`() {
        val state = createInitialState().copy(
            phoneNumber = TextInputComponentState(
                error = TextInputComponentState.InputError(CheckoutLocalizationKey.GENERAL_CLOSE, isVisible = true),
            ),
            focusRequest = null,
        )

        val actual = reducer.reduce(state, MBWayIntent.UpdateFieldFocus(MBWayFieldId.PHONE_NUMBER, hasFocus = true))

        assertFalse(actual.phoneNumber.isErrorVisible)
    }

    @Test
    fun `when pay is pressed and the phone number is invalid, then its error shows and it is asked for focus`() {
        val state = createInitialState().copy(
            phoneNumber = TextInputComponentState(
                text = "",
                error = TextInputComponentState.InputError(CheckoutLocalizationKey.GENERAL_CLOSE),
            ),
        )

        val actual = reducer.reduce(state, MBWayIntent.HighlightValidationErrors)

        assertTrue(actual.phoneNumber.isErrorVisible)
        assertEquals(FocusRequest(MBWayFieldId.PHONE_NUMBER, keepErrorHighlight = true), actual.focusRequest)
    }

    @Test
    fun `when pay is pressed and the phone number is valid, then nothing shows and no focus is asked for`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, MBWayIntent.HighlightValidationErrors)

        assertFalse(actual.phoneNumber.isErrorVisible)
        assertNull(actual.focusRequest)
    }

    @Test
    fun `when the country picker cannot be focused, then asking to focus it changes nothing`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, MBWayIntent.UpdateFieldFocus(MBWayFieldId.COUNTRY_CODE, hasFocus = true))

        assertEquals(state, actual)
    }

    private fun createInitialState() = MBWayComponentState(
        countries = emptyList(),
        selectedCountryCode = CountryModel("NL", "Netherlands", "+31"),
        phoneNumber = TextInputComponentState(
            text = "",
            isFocused = false,
            error = null
        ),
        isLoading = false,
    )
}
