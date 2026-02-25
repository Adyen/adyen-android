/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 25/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.state

import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.data.provider.TestSdkDataProvider
import com.adyen.checkout.googlepay.internal.helper.GooglePayUtils
import com.google.android.gms.wallet.PaymentData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class GooglePayComponentStateExtTest {

    private lateinit var sdkDataProvider: TestSdkDataProvider

    @Mock
    private lateinit var paymentData: PaymentData

    @BeforeEach
    fun beforeEach() {
        sdkDataProvider = TestSdkDataProvider()
    }

    @Test
    fun `when toPaymentComponentState is called with valid payment data, then a valid component state is created`() {
        whenever(paymentData.toJson()).thenReturn(TEST_PAYMENT_DATA_JSON)
        val amount = Amount("EUR", 1337)
        val paymentMethodType = "googlepay"
        val componentState = GooglePayComponentState(
            isButtonVisible = true,
            isLoading = false,
            isAvailable = true,
            paymentData = paymentData,
        )

        val paymentComponentState = componentState.toPaymentComponentState(
            amount = amount,
            paymentMethodType = paymentMethodType,
            sdkDataProvider = sdkDataProvider,
        )

        val expectedPaymentMethod = GooglePayUtils.createGooglePayPaymentMethod(
            paymentData = paymentData,
            paymentMethodType = paymentMethodType,
            sdkData = TestSdkDataProvider.TEST_SDK_DATA,
        )

        assertEquals(expectedPaymentMethod, paymentComponentState.data.paymentMethod)
        assertEquals(amount, paymentComponentState.data.amount)
        assertNull(paymentComponentState.data.order)
        assertTrue(paymentComponentState.isValid)
    }

    @Test
    fun `when paymentData is null, then state is not valid`() {
        val componentState = GooglePayComponentState(
            isButtonVisible = true,
            isLoading = false,
            isAvailable = true,
            paymentData = null,
        )

        val paymentComponentState = componentState.toPaymentComponentState(
            amount = null,
            paymentMethodType = "googlepay",
            sdkDataProvider = sdkDataProvider,
        )

        assertFalse(paymentComponentState.isValid)
        assertNull(paymentComponentState.data.paymentMethod)
    }

    @Test
    fun `when amount is null, then payment component data has null amount`() {
        whenever(paymentData.toJson()).thenReturn(TEST_PAYMENT_DATA_JSON)
        val componentState = GooglePayComponentState(
            isButtonVisible = true,
            isLoading = false,
            isAvailable = true,
            paymentData = paymentData,
        )

        val paymentComponentState = componentState.toPaymentComponentState(
            amount = null,
            paymentMethodType = "googlepay",
            sdkDataProvider = sdkDataProvider,
        )

        assertNull(paymentComponentState.data.amount)
    }

    companion object {
        private const val TEST_PAYMENT_DATA_JSON =
            "{\"paymentMethodData\": {\"tokenizationData\": {\"token\": \"test_token\"}}}"
    }
}
