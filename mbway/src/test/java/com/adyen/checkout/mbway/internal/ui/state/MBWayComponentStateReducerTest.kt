package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
    fun `when intent is UpdatePhoneNumberFocus, then state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, MBWayIntent.UpdatePhoneNumberFocus(true))

        val expected = state.copy(phoneNumber = state.phoneNumber.updateFocus(true))
        assertEquals(expected, actual)
    }

    @Test
    fun `when intent is HighlightValidationErrors and phone number has error, then state is updated`() {
        val state = createInitialState().copy(
            phoneNumber = TextInputComponentState(
                text = "",
                isFocused = false,
                errorMessage = CheckoutLocalizationKey.GENERAL_CLOSE,
                showError = false,
            ),
        )

        val actual = reducer.reduce(state, MBWayIntent.HighlightValidationErrors)

        assertTrue(actual.phoneNumber.showError)
        assertTrue(actual.phoneNumber.isFocused)
    }

    @Test
    fun `when intent is HighlightValidationErrors and phone number has no error, then state is not updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, MBWayIntent.HighlightValidationErrors)

        assertFalse(actual.phoneNumber.showError)
        assertFalse(actual.phoneNumber.isFocused)
    }

    private fun createInitialState() = MBWayComponentState(
        countries = emptyList(),
        selectedCountryCode = CountryModel("NL", "Netherlands", "+31"),
        phoneNumber = TextInputComponentState(
            text = "",
            isFocused = false,
            errorMessage = null,
            showError = false,
        ),
        isLoading = false,
    )
}
