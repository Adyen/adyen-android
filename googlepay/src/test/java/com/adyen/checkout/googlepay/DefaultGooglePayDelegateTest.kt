/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/7/2022.
 */

package com.adyen.checkout.googlepay

import app.cash.turbine.test
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.GooglePayPaymentMethod
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.google.android.gms.wallet.PaymentData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultGooglePayDelegateTest {

    private val delegate = DefaultGooglePayDelegate(
        paymentMethod = PaymentMethod(),
        configuration = GooglePayConfiguration.Builder(
            Locale.US,
            Environment.TEST,
            "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        ).build(),
    )

    private val paymentData: PaymentData
        get() = PaymentData.fromJson("{\"paymentMethodData\": {\"tokenizationData\": {\"token\": \"test_token\"}}}")

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `payment data in null, then an exception should be thrown`() {
            assertThrows(CheckoutException::class.java) {
                delegate.onInputDataChanged(GooglePayInputData(null))
            }
        }

        @Test
        fun `everything is good, then output data should be propagated`() = runTest {
            delegate.outputDataFlow.test {
                skipItems(1)

                val paymentData = paymentData

                delegate.onInputDataChanged(GooglePayInputData(paymentData))

                assertEquals(paymentData, awaitItem()?.paymentData)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `when creating component state is successful, then the state is propagated`() = runTest {
        delegate.componentStateFlow.test {
            skipItems(1)

            val paymentData = paymentData

            delegate.createComponentState(GooglePayOutputData(paymentData))

            val componentState = awaitItem()
            assertTrue(componentState!!.data.paymentMethod is GooglePayPaymentMethod)
            assertTrue(componentState.isInputValid)
            assertTrue(componentState.isReady)
            assertEquals(paymentData, componentState.paymentData)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
