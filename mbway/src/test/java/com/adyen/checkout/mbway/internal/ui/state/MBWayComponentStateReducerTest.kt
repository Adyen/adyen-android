package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
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

    private fun createInitialState() = MBWayComponentState(
        countries = emptyList(),
        selectedCountryCode = CountryModel("NL", "Netherlands", "+31"),
        phoneNumber = TextInputComponentState(),
        isLoading = false,
    )
}
