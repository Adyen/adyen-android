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
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.core.api.Environment
import com.google.android.gms.wallet.PaymentData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultGooglePayDelegateTest {

    private lateinit var delegate: DefaultGooglePayDelegate

    private val paymentData: PaymentData
        get() = PaymentData.fromJson("{\"paymentMethodData\": {\"tokenizationData\": {\"token\": \"test_token\"}}}")

    @BeforeEach
    fun beforeEach() {
        delegate = DefaultGooglePayDelegate(
            observerRepository = PaymentObserverRepository(),
            paymentMethod = PaymentMethod(),
            configuration = GooglePayConfiguration.Builder(
                Locale.US,
                Environment.TEST,
                "test_qwertyuiopasdfghjklzxcvbnmqwerty"
            ).build(),
        )
    }

    @Test
    fun `when delegate is initialized, then state is not valid`() = runTest {
        delegate.componentStateFlow.test {
            with(awaitItem()) {
                assertNull(data.paymentMethod)
                assertFalse(isInputValid)
                assertTrue(isReady)
                assertNull(paymentData)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when payment data is null, then state is not valid`() = runTest {
        delegate.componentStateFlow.test {
            skipItems(1)

            delegate.updateComponentState(null)

            with(awaitItem()) {
                assertNull(data.paymentMethod)
                assertFalse(isInputValid)
                assertTrue(isReady)
                assertNull(paymentData)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when creating component state with valid payment data, then the state is propagated`() = runTest {
        delegate.componentStateFlow.test {
            skipItems(1)

            val paymentData = paymentData

            delegate.updateComponentState(paymentData)

            with(awaitItem()) {
                assertTrue(data.paymentMethod is GooglePayPaymentMethod)
                assertTrue(isInputValid)
                assertTrue(isReady)
                assertEquals(paymentData, paymentData)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }
}
