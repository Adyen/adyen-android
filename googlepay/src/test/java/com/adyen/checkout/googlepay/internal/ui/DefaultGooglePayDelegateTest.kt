/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/7/2022.
 */

package com.adyen.checkout.googlepay.internal.ui

import app.cash.turbine.test
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.Configuration
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.paymentmethod.GooglePayPaymentMethod
import com.adyen.checkout.core.Environment
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParamsMapper
import com.google.android.gms.wallet.PaymentData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultGooglePayDelegateTest(
    @Mock private val analyticsRepository: AnalyticsRepository,
) {

    private lateinit var delegate: DefaultGooglePayDelegate

    private val paymentData: PaymentData
        get() = PaymentData.fromJson("{\"paymentMethodData\": {\"tokenizationData\": {\"token\": \"test_token\"}}}")

    @BeforeEach
    fun beforeEach() {
        delegate = createGooglePayDelegate()
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
                assertEquals(TEST_ORDER, data.order)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @ParameterizedTest
    @MethodSource("amountSource")
    fun `when input data is valid then amount is propagated in component state if set`(
        configurationValue: Amount?,
        expectedComponentStateValue: Amount?,
    ) = runTest {
        if (configurationValue != null) {
            val configuration = getGooglePayConfigurationBuilder()
                .setAmount(configurationValue)
                .build()
            delegate = createGooglePayDelegate(configuration = configuration)
        }
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        delegate.componentStateFlow.test {
            delegate.updateComponentState(paymentData)
            assertEquals(expectedComponentStateValue, expectMostRecentItem().data.amount)
        }
    }

    @Test
    fun `when delegate is initialized then analytics event is sent`() = runTest {
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        verify(analyticsRepository).sendAnalyticsEvent()
    }

    private fun getGooglePayConfigurationBuilder(): GooglePayConfiguration.Builder {
        return GooglePayConfiguration.Builder(
            Locale.US,
            Environment.TEST,
            "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        )
    }

    private fun createGooglePayDelegate(
        configuration: GooglePayConfiguration = getGooglePayConfigurationBuilder().build(),
        paymentMethod: PaymentMethod = PaymentMethod(
            configuration = Configuration(gatewayMerchantId = "TEST_GATEWAY_MERCHANT_ID")
        ),
    ): DefaultGooglePayDelegate {
        return DefaultGooglePayDelegate(
            observerRepository = PaymentObserverRepository(),
            paymentMethod = PaymentMethod(),
            order = TEST_ORDER,
            componentParams = GooglePayComponentParamsMapper(null, null).mapToParams(
                configuration,
                paymentMethod,
                null
            ),
            analyticsRepository = analyticsRepository,
        )
    }

    companion object {
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, expectedComponentStateValue
            arguments(Amount("EUR", 100), Amount("EUR", 100)),
            arguments(Amount("USD", 0), Amount("USD", 0)),
            arguments(Amount.EMPTY, Amount("USD", 0)),
            arguments(null, Amount("USD", 0)),
        )
    }
}
