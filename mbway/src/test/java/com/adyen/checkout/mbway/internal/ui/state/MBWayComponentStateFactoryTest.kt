package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Locale

@ExtendWith(MockitoExtension::class)
internal class MBWayComponentStateFactoryTest {

    private lateinit var factory: MBWayComponentStateFactory

    @BeforeEach
    fun beforeEach() {
        factory = MBWayComponentStateFactory(Locale.US)
    }

    @Test
    fun `when creating initial state, then conform with expected state`() {
        val countryPT = CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351")
        val countryES = CountryModel(isoCode = "ES", countryName = "Spain", callingCode = "+34")
        val supportedCountries = listOf(countryPT, countryES)

        val actual = factory.createInitialState()

        val expected = MBWayComponentState(
            countries = supportedCountries,
            selectedCountryCode = countryPT,
            phoneNumber = TextInputComponentState(
                text = "",
                description = null,
                errorMessage = null,
                isFocused = true,
                showError = false,
            ),
            isLoading = false,
        )
        assertEquals(expected, actual)
    }
}
