package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MBWayViewStateProducerTest {

    private lateinit var producer: MBWayViewStateProducer

    @BeforeEach
    fun beforeEach() {
        producer = MBWayViewStateProducer()
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
                isFocused = true,
                errorMessage = CheckoutLocalizationKey.GENERAL_CLOSE,
                showError = true,
            ),
            isLoading = true,
        )

        val actual = producer.produce(componentState)

        val expected = MBWayViewState(
            countries = componentState.countries,
            selectedCountryCode = componentState.selectedCountryCode,
            phoneNumber = TextInputViewState(
                text = "123456789",
                isFocused = true,
                supportingText = CheckoutLocalizationKey.GENERAL_CLOSE,
                isError = true,
            ),
            isLoading = true,
        )

        assertEquals(expected, actual)
    }
}
