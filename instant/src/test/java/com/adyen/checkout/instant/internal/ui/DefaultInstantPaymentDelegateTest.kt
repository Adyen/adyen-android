/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 9/11/2022.
 */

package com.adyen.checkout.instant.internal.ui

import app.cash.turbine.test
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.Environment
import com.adyen.checkout.instant.internal.ui.model.InstantComponentParamsMapper
import com.adyen.checkout.test.LoggingExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, LoggingExtension::class)
class DefaultInstantPaymentDelegateTest(
    @Mock private val analyticsRepository: AnalyticsRepository,
) {

    private lateinit var delegate: DefaultInstantPaymentDelegate

    @BeforeEach
    fun before() {
        delegate = createInstantPaymentDelegate()
    }

    @Test
    fun `when subscribed then component state flow should propagate a valid state`() = runTest {
        delegate.componentStateFlow.test {
            with(awaitItem()) {
                assertEquals(TYPE, data.paymentMethod?.type)
                assertEquals(TEST_ORDER, data.order)
                assertTrue(isInputValid)
                assertTrue(isValid)
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
            val configuration = createCheckoutConfiguration(configurationValue)
            delegate = createInstantPaymentDelegate(configuration = configuration)
        }
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        delegate.componentStateFlow.test {
            assertEquals(expectedComponentStateValue, expectMostRecentItem().data.amount)
        }
    }

    @Test
    fun `when delegate is initialized then analytics event is sent`() = runTest {
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        verify(analyticsRepository).setupAnalytics()
    }

    @Nested
    inner class AnalyticsTest {

        @Test
        fun `when component state is valid then PaymentMethodDetails should contain checkoutAttemptId`() = runTest {
            whenever(analyticsRepository.getCheckoutAttemptId()) doReturn TEST_CHECKOUT_ATTEMPT_ID

            delegate = createInstantPaymentDelegate()

            delegate.componentStateFlow.test {
                assertEquals(TEST_CHECKOUT_ATTEMPT_ID, expectMostRecentItem().data.paymentMethod?.checkoutAttemptId)
            }
        }
    }

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        amount = amount,
    )

    private fun createInstantPaymentDelegate(
        configuration: CheckoutConfiguration = createCheckoutConfiguration(),
    ): DefaultInstantPaymentDelegate {
        return DefaultInstantPaymentDelegate(
            observerRepository = PaymentObserverRepository(),
            paymentMethod = PaymentMethod(type = TYPE),
            order = TEST_ORDER,
            componentParams = InstantComponentParamsMapper(CommonComponentParamsMapper())
                .mapToParams(configuration, Locale.US, null, null, PaymentMethod(type = "paypal")),
            analyticsRepository = analyticsRepository,
        )
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TYPE = "txVariant"
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
        private const val TEST_CHECKOUT_ATTEMPT_ID = "TEST_CHECKOUT_ATTEMPT_ID"

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
