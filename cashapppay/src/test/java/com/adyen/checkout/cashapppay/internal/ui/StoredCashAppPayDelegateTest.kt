/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/7/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui

import com.adyen.checkout.cashapppay.CashAppPayConfiguration
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayComponentParamsMapper
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.core.Environment
import com.adyen.checkout.test.extensions.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
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
internal class StoredCashAppPayDelegateTest(
    @Mock private val analyticsRepository: AnalyticsRepository,
) {

    private lateinit var delegate: StoredCashAppPayDelegate

    @BeforeEach
    fun before() {
        delegate = createStoredCashAppPayDelegate()
    }

    @Test
    fun `when delegate is initialized, then state is valid`() = runTest {
        val testFlow = delegate.componentStateFlow.test(testScheduler)
        with(testFlow.latestValue) {
            assertTrue(isInputValid)
            assertTrue(isReady)
            assertTrue(isValid)
        }
    }

    @Test
    fun `when delegate is initialized, then analytics event is sent`() = runTest {
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        verify(analyticsRepository).sendAnalyticsEvent()
    }

    @ParameterizedTest
    @MethodSource("amountSource")
    fun `when input data is valid, then amount is propagated in component state if set`(
        configurationValue: Amount?,
        expectedComponentStateValue: Amount?,
    ) = runTest {
        if (configurationValue != null) {
            val configuration = getConfigurationBuilder()
                .setAmount(configurationValue)
                .build()
            delegate = createStoredCashAppPayDelegate(configuration = configuration)
        }
        val testFlow = delegate.componentStateFlow.test(testScheduler)

        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        assertEquals(expectedComponentStateValue, testFlow.latestValue.data.amount)
    }

    @Test
    fun `when delegate is initialized, then submit handler onSubmit is called`() = runTest {
        val testFlow = delegate.submitFlow.test(testScheduler)

        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        assertEquals(delegate.componentStateFlow.first(), testFlow.latestValue)
    }

    private fun createStoredCashAppPayDelegate(
        configuration: CashAppPayConfiguration = getConfigurationBuilder().build()
    ) = StoredCashAppPayDelegate(
        analyticsRepository = analyticsRepository,
        observerRepository = PaymentObserverRepository(),
        paymentMethod = StoredPaymentMethod(),
        order = TEST_ORDER,
        componentParams = CashAppPayComponentParamsMapper(null, null).mapToParams(
            configuration = configuration,
            sessionParams = null,
            paymentMethod = StoredPaymentMethod(),
        ),
    )

    private fun getConfigurationBuilder() = CashAppPayConfiguration.Builder(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = "test_qwertyuiopasdfghjklzxcvbnmqwerty",
    )
        .setReturnUrl("test")

    companion object {
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, expectedComponentStateValue
            arguments(Amount("EUR", 100), Amount("EUR", 100)),
            arguments(Amount("USD", 0), Amount("USD", 0)),
            arguments(Amount.EMPTY, null),
            arguments(null, null),
        )
    }
}
