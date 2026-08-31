package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.form.KeyboardAction
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MBWayViewStateProducerTest {

    private lateinit var producer: MBWayViewStateProducer

    @BeforeEach
    fun beforeEach() {
        producer = MBWayViewStateProducer(amount = TEST_AMOUNT, showSubmitButton = true)
    }

    @Test
    fun `when produce is called, then view state is created`() {
        val componentState = MBWayComponentState(
            countries = listOf(
                CountryModel("PT", "Portugal", "351"),
                CountryModel("ES", "Spain", "34"),
            ),
            selectedCountryCode = CountryModel("PT", "351", "Portugal"),
            phoneNumber = TextInputComponentState(
                text = "123456789",
                error = TextInputComponentState.InputError(CheckoutLocalizationKey.GENERAL_CLOSE, isVisible = true)
            ),
            isLoading = true,
        )

        val actual = producer.produce(componentState)

        val expected = MBWayViewState(
            elements = listOf(
                MBWayFormElement.CountryCode(selectedCountry = componentState.selectedCountryCode),
                MBWayFormElement.PhoneNumber(
                    textInputViewState = TextInputViewState(
                        text = "123456789",
                        supportingText = CheckoutLocalizationKey.GENERAL_CLOSE,
                        isError = true,
                        // The only text input of the form, so it closes the keyboard rather than moving on.
                        keyboardAction = KeyboardAction.DONE,
                    ),
                    callingCode = componentState.selectedCountryCode.callingCode,
                ),
            ),
            isLoading = true,
            payButtonViewState = PayButtonViewState(amount = TEST_AMOUNT, isLoading = true),
            countryPickerViewState = CountryPickerViewState(
                countries = componentState.countries,
                selectedCountry = componentState.selectedCountryCode,
            ),
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `when produce is called, then the country picker comes before the phone number`() {
        val country = CountryModel("PT", "Portugal", "351")
        val componentState = MBWayComponentState(
            countries = listOf(country),
            selectedCountryCode = country,
            phoneNumber = TextInputComponentState(),
            isLoading = false,
        )

        val actual = producer.produce(componentState)

        assertEquals(
            listOf(MBWayFieldId.COUNTRY_CODE, MBWayFieldId.PHONE_NUMBER),
            actual.elements.map { it.id },
        )
    }

    @Test
    fun `when show submit button is false then pay button view state is null`() {
        val producer = MBWayViewStateProducer(amount = TEST_AMOUNT, showSubmitButton = false)
        val country = CountryModel("PT", "Portugal", "351")
        val componentState = MBWayComponentState(
            countries = listOf(country),
            selectedCountryCode = country,
            phoneNumber = TextInputComponentState(),
            isLoading = false,
        )

        val actual = producer.produce(componentState)

        assertNull(actual.payButtonViewState)
    }

    companion object {
        private val TEST_AMOUNT = Amount(currency = "EUR", value = 1337)
    }
}
