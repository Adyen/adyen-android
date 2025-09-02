package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputState
import com.adyen.checkout.mbway.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class MBWayViewStateValidatorTest {

    private lateinit var validator: MBWayViewStateValidator

    @BeforeEach
    fun setup() {
        validator = MBWayViewStateValidator()
    }

    @Nested
    inner class ValidateTest {

        @Test
        fun `when phone number is valid, then validate should return state without error message`() {
            val viewState = MBWayViewState(
                countries = emptyList(),
                countryCode = CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351"),
                phoneNumber = TextInputState(
                    text = VALID_PHONE_NUMBER,
                    errorMessage = null,
                    isFocused = false,
                    isEdited = true,
                    showError = false,
                ),
                isLoading = false,
            )

            val actual = validator.validate(viewState)

            assertEquals(null, actual.phoneNumber.errorMessage)
        }

        @Test
        fun `when phone number is invalid, then validate should return state with error message`() {
            val viewState = MBWayViewState(
                countries = emptyList(),
                countryCode = CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351"),
                phoneNumber = TextInputState(
                    text = INVALID_PHONE_NUMBER,
                    errorMessage = null,
                    isFocused = false,
                    isEdited = true,
                    showError = false,
                ),
                isLoading = false,
            )

            val actual = validator.validate(viewState)

            assertEquals(R.string.checkout_mbway_phone_number_not_valid, actual.phoneNumber.errorMessage)
        }
    }

    @Nested
    inner class IsValidTest {

        @Test
        fun `when phone number is valid, then isValid should return true`() {
            val viewState = MBWayViewState(
                countries = emptyList(),
                countryCode = CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351"),
                phoneNumber = TextInputState(
                    text = VALID_PHONE_NUMBER,
                    errorMessage = null,
                    isFocused = false,
                    isEdited = true,
                    showError = false,
                ),
                isLoading = false,
            )

            val actual = validator.isValid(viewState)

            assertTrue(actual)
        }

        @Test
        fun `when phone number is invalid, then isValid should return false`() {
            val viewState = MBWayViewState(
                countries = emptyList(),
                countryCode = CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351"),
                phoneNumber = TextInputState(
                    text = INVALID_PHONE_NUMBER,
                    errorMessage = null,
                    isFocused = false,
                    isEdited = true,
                    showError = false,
                ),
                isLoading = false,
            )

            val actual = validator.isValid(viewState)

            assertFalse(actual)
        }
    }

    @Nested
    inner class HighlightAllValidationErrorsTest {

        @Test
        fun `when phone number is valid, then highlightAllValidationErrors should not show error`() {
            val viewState = MBWayViewState(
                countries = emptyList(),
                countryCode = CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351"),
                phoneNumber = TextInputState(
                    text = VALID_PHONE_NUMBER,
                    errorMessage = null,
                    isFocused = false,
                    isEdited = true,
                    showError = false,
                ),
                isLoading = false,
            )

            val actual = validator.highlightAllValidationErrors(viewState)

            assertFalse(actual.phoneNumber.showError)
            assertEquals(null, actual.phoneNumber.errorMessage)
        }

        @Test
        fun `when phone number is invalid, then highlightAllValidationErrors should show error and focus`() {
            val viewState = MBWayViewState(
                countries = emptyList(),
                countryCode = CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351"),
                phoneNumber = TextInputState(
                    text = INVALID_PHONE_NUMBER,
                    errorMessage = null,
                    isFocused = false,
                    isEdited = true,
                    showError = false,
                ),
                isLoading = false,
            )

            val actual = validator.highlightAllValidationErrors(viewState)

            assertTrue(actual.phoneNumber.showError)
            assertEquals(R.string.checkout_mbway_phone_number_not_valid, actual.phoneNumber.errorMessage)
            assertTrue(actual.phoneNumber.isFocused)
        }
    }

    companion object {
        private const val VALID_PHONE_NUMBER = "912345678"
        private const val INVALID_PHONE_NUMBER = "123"
    }
}
