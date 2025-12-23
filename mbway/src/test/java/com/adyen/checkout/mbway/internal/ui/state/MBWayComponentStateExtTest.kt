package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.data.provider.TestSdkDataProvider
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.paymentmethod.MBWayPaymentMethod
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MBWayComponentStateExtTest {

    private lateinit var sdkDataProvider: TestSdkDataProvider

    @BeforeEach
    fun beforeEach() {
        sdkDataProvider = TestSdkDataProvider()
    }

    @Test
    fun `when toPaymentComponentState is called, then a valid component state is created`() {
        val amount = Amount("EUR", 1337)
        val componentState = MBWayComponentState(
            countries = emptyList(),
            selectedCountryCode = CountryModel("PT", "Portugal", "+351"),
            phoneNumber = TextInputComponentState(
                text = "123456789",
            ),
            isLoading = false,
        )

        val paymentComponentState = componentState.toPaymentComponentState(
            amount = amount,
            sdkDataProvider = sdkDataProvider,
        )

        val expectedPaymentMethod = MBWayPaymentMethod(
            type = MBWayPaymentMethod.PAYMENT_METHOD_TYPE,
            sdkData = TestSdkDataProvider.TEST_SDK_DATA,
            telephoneNumber = "+351123456789",
        )
        val expectedPaymentComponentData = PaymentComponentData(
            paymentMethod = expectedPaymentMethod,
            order = null,
            amount = amount,
        )
        val expectedPaymentComponentState = MBWayPaymentComponentState(
            data = expectedPaymentComponentData,
            isValid = true,
        )

        assertEquals(expectedPaymentComponentState, paymentComponentState)
        assertTrue(paymentComponentState.isValid)
    }

    @Test
    fun `when phone number has leading zeroes, then they are removed`() {
        val componentState = MBWayComponentState(
            countries = emptyList(),
            selectedCountryCode = CountryModel("PT", "Portugal", "+351"),
            phoneNumber = TextInputComponentState(
                text = "00123456789",
            ),
            isLoading = false,
        )

        val paymentComponentState = componentState.toPaymentComponentState(null, sdkDataProvider)

        assertEquals(
            "+351123456789",
            paymentComponentState.data.paymentMethod?.telephoneNumber,
        )
    }
}
