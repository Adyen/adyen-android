package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class MBWayComponentStateValidatorTest {

    private lateinit var validator: MBWayComponentStateValidator

    @BeforeEach
    fun beforeEach() {
        validator = MBWayComponentStateValidator()
    }

    @Nested
    inner class ValidateTest {

        @Test
        fun `when phone number is valid, then validate should return state without error message`() {
            val state = MBWayComponentState(
                countries = emptyList(),
                selectedCountryCode = CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351"),
                phoneNumber = TextInputComponentState(
                    text = VALID_PHONE_NUMBER,
                    errorMessage = null,
                    isFocused = false,
                    showError = false,
                ),
                isLoading = false,
            )

            val actual = validator.validate(state)

            assertNull(actual.phoneNumber.errorMessage)
        }

        @Test
        fun `when phone number is invalid, then validate should return state with error message`() {
            val viewState = MBWayComponentState(
                countries = emptyList(),
                selectedCountryCode = CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351"),
                phoneNumber = TextInputComponentState(
                    text = INVALID_PHONE_NUMBER,
                    errorMessage = null,
                    isFocused = false,
                    showError = false,
                ),
                isLoading = false,
            )

            val actual = validator.validate(viewState)

            assertEquals(CheckoutLocalizationKey.MBWAY_INVALID_PHONE_NUMBER, actual.phoneNumber.errorMessage)
        }
    }

    @Nested
    inner class IsValidTest {

        @Test
        fun `when phone number is valid, then isValid should return true`() {
            val viewState = MBWayComponentState(
                countries = emptyList(),
                selectedCountryCode = CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351"),
                phoneNumber = TextInputComponentState(
                    text = VALID_PHONE_NUMBER,
                    errorMessage = null,
                    isFocused = false,
                    showError = false,
                ),
                isLoading = false,
            )

            val actual = validator.isValid(viewState)

            assertTrue(actual)
        }

        @Test
        fun `when phone number has an error, then isValid should return false`() {
            val viewState = MBWayComponentState(
                countries = emptyList(),
                selectedCountryCode = CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351"),
                phoneNumber = TextInputComponentState(
                    text = INVALID_PHONE_NUMBER,
                    errorMessage = CheckoutLocalizationKey.MBWAY_INVALID_PHONE_NUMBER,
                    isFocused = false,
                    showError = false,
                ),
                isLoading = false,
            )

            val actual = validator.isValid(viewState)

            assertFalse(actual)
        }
    }

    companion object {
        private const val VALID_PHONE_NUMBER = "912345678"
        private const val INVALID_PHONE_NUMBER = "123"
    }
}
