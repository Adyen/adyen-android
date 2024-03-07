/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/7/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui

import android.app.Application
import com.adyen.checkout.cashapppay.CashAppPayComponentState
import com.adyen.checkout.cashapppay.cashAppPay
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayComponentParamsMapper
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.paymentmethod.CashAppPayPaymentMethod
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
        verify(analyticsRepository).setupAnalytics()
    }

    @ParameterizedTest
    @MethodSource("amountSource")
    fun `when input data is valid, then amount is propagated in component state if set`(
        configurationValue: Amount?,
        expectedComponentStateValue: Amount?,
    ) = runTest {
        if (configurationValue != null) {
            val configuration = createCheckoutConfiguration(configurationValue)
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

    @Test
    fun `when delegate is initialized, then component state is created correctly`() = runTest {
        val testFlow = delegate.componentStateFlow.test(testScheduler)

        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        val expected = CashAppPayComponentState(
            data = PaymentComponentData(
                paymentMethod = CashAppPayPaymentMethod(
                    type = TEST_PAYMENT_METHOD_TYPE,
                    checkoutAttemptId = null,
                    storedPaymentMethodId = TEST_PAYMENT_METHOD_ID,
                ),
                order = TEST_ORDER,
                amount = null,
            ),
            isInputValid = true,
            isReady = true,
        )
        assertEquals(expected, testFlow.latestValue)
    }

    private fun createStoredCashAppPayDelegate(
        configuration: CheckoutConfiguration = createCheckoutConfiguration()
    ) = StoredCashAppPayDelegate(
        analyticsRepository = analyticsRepository,
        observerRepository = PaymentObserverRepository(),
        paymentMethod = StoredPaymentMethod(
            id = TEST_PAYMENT_METHOD_ID,
            type = TEST_PAYMENT_METHOD_TYPE,
        ),
        order = TEST_ORDER,
        componentParams = CashAppPayComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = Locale.US,
            dropInOverrideParams = null,
            componentSessionParams = null,
            storedPaymentMethod = StoredPaymentMethod(),
            context = Application(),
        ),
    )

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = "test_qwertyuiopasdfghjklzxcvbnmqwerty",
        amount = amount,
    ) {
        cashAppPay {
            setReturnUrl(TEST_RETURN_URL)
        }
    }

    companion object {
        private const val TEST_RETURN_URL = "testReturnUrl"
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
        private const val TEST_PAYMENT_METHOD_ID = "TEST_PAYMENT_METHOD_ID"
        private const val TEST_PAYMENT_METHOD_TYPE = "TEST_PAYMENT_METHOD_TYPE"

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, expectedComponentStateValue
            arguments(Amount("EUR", 100), Amount("EUR", 100)),
            arguments(Amount("USD", 0), Amount("USD", 0)),
            arguments(null, null),
            arguments(null, null),
        )
    }
}
