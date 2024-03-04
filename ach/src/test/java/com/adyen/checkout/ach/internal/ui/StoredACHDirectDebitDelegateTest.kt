/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 28/2/2023.
 */

package com.adyen.checkout.ach.internal.ui

import app.cash.turbine.test
import com.adyen.checkout.ach.ACHDirectDebitConfiguration
import com.adyen.checkout.ach.achDirectDebit
import com.adyen.checkout.ach.internal.ui.model.ACHDirectDebitComponentParamsMapper
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.OldAnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.Environment
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class StoredACHDirectDebitDelegateTest(
    @Mock private val analyticsRepository: OldAnalyticsRepository
) {
    private lateinit var delegate: ACHDirectDebitDelegate

    @BeforeEach
    fun setUp() {
        delegate = createAchDelegate()
    }

    @Nested
    @DisplayName("when creating component state and")
    inner class CreateComponentStateTest {
        @Test
        fun `when delegate is created , then component state should be valid`() = runTest {
            val componentState = delegate.componentStateFlow.first()
            assertTrue(componentState.isValid)
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.ach.internal.ui.StoredACHDirectDebitDelegateTest#amountSource")
        fun `when input data is valid then amount is propagated in component state if set`(
            configurationValue: Amount?,
            expectedComponentStateValue: Amount?,
        ) = runTest {
            if (configurationValue != null) {
                val configuration = createCheckoutConfiguration(configurationValue)
                delegate = createAchDelegate(configuration = configuration)
            }
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.componentStateFlow.test {
                assertEquals(expectedComponentStateValue, expectMostRecentItem().data.amount)
            }
        }
    }

    @Test
    fun `when delegate is initialized then submit handler onSubmit is called`() = runTest {
        delegate.submitFlow.test {
            val coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
            delegate.initialize(coroutineScope)
            assertEquals(delegate.componentStateFlow.first(), expectMostRecentItem())
        }
    }

    @Nested
    inner class AnalyticsTest {

        @Test
        fun `when component state is valid then PaymentMethodDetails should contain checkoutAttemptId`() = runTest {
            whenever(analyticsRepository.getCheckoutAttemptId()) doReturn TEST_CHECKOUT_ATTEMPT_ID

            delegate = createAchDelegate()

            delegate.componentStateFlow.test {
                assertEquals(TEST_CHECKOUT_ATTEMPT_ID, expectMostRecentItem().data.paymentMethod?.checkoutAttemptId)
            }
        }
    }

    private fun createAchDelegate(
        paymentMethod: StoredPaymentMethod = StoredPaymentMethod(id = STORED_ID),
        analyticsRepository: OldAnalyticsRepository = this.analyticsRepository,
        configuration: CheckoutConfiguration = createCheckoutConfiguration(),
        order: OrderRequest? = TEST_ORDER,
    ) = StoredACHDirectDebitDelegate(
        observerRepository = PaymentObserverRepository(),
        storedPaymentMethod = paymentMethod,
        analyticsRepository = analyticsRepository,
        componentParams = ACHDirectDebitComponentParamsMapper(CommonComponentParamsMapper())
            .mapToParams(configuration, DEVICE_LOCALE, null, null),
        order = order,
    )

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: ACHDirectDebitConfiguration.Builder.() -> Unit = {}
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        amount = amount,
    ) {
        achDirectDebit(configuration)
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
        private const val STORED_ID = "Stored_id"
        private const val TEST_CHECKOUT_ATTEMPT_ID = "TEST_CHECKOUT_ATTEMPT_ID"
        private val DEVICE_LOCALE = Locale("nl", "NL")

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
