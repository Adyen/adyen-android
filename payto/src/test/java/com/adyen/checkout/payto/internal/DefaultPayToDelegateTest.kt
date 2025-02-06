/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 5/2/2025.
 */

package com.adyen.checkout.payto.internal

import app.cash.turbine.test
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.Environment
import com.adyen.checkout.payto.PayToComponentState
import com.adyen.checkout.payto.PayToConfiguration
import com.adyen.checkout.payto.getPayToConfiguration
import com.adyen.checkout.payto.internal.ui.DefaultPayToDelegate
import com.adyen.checkout.payto.payTo
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
class DefaultPayToDelegateTest(
    @Mock private val submitHandler: SubmitHandler<PayToComponentState>,
) {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var delegate: DefaultPayToDelegate

    @BeforeEach
    fun beforeEach() {
        analyticsManager = TestAnalyticsManager()
        delegate = createPayToDelegate()
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {
        // TODO To be added later
    }

    @Nested
    @DisplayName("when creating component state and")
    inner class CreateComponentStateTest {
        // TODO To be added later
    }

    @Nested
    inner class SubmitButtonVisibilityTest {

        @Test
        fun `when submit button is configured to be hidden, then it should not show`() {
            delegate = createPayToDelegate(
                configuration = createCheckoutConfiguration {
                    setSubmitButtonVisible(false)
                },
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `when submit button is configured to be visible, then it should show`() {
            delegate = createPayToDelegate(
                configuration = createCheckoutConfiguration {
                    setSubmitButtonVisible(true)
                },
            )

            assertTrue(delegate.shouldShowSubmitButton())
        }
    }

    @Nested
    inner class SubmitHandlerTest {

        @Test
        fun `when delegate is initialized then submit handler event is initialized`() = runTest {
            val coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
            delegate.initialize(coroutineScope)
            verify(submitHandler).initialize(coroutineScope, delegate.componentStateFlow)
        }

        @Test
        fun `when delegate setInteractionBlocked is called then submit handler setInteractionBlocked is called`() =
            runTest {
                delegate.setInteractionBlocked(true)
                verify(submitHandler).setInteractionBlocked(true)
            }

        @Test
        fun `when delegate onSubmit is called then submit handler onSubmit is called`() = runTest {
            delegate.componentStateFlow.test {
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
                delegate.onSubmit()
                verify(submitHandler).onSubmit(expectMostRecentItem())
            }
        }
    }

    @Nested
    inner class AnalyticsTest {

        @Test
        fun `when delegate is initialized then analytics manager is initialized`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            analyticsManager.assertIsInitialized()
        }

        @Test
        fun `when delegate is initialized, then render event is tracked`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val expectedEvent = GenericEvents.rendered(TEST_PAYMENT_METHOD_TYPE)
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when submitFlow emits an event, then submit event is tracked`() = runTest {
            val submitFlow = flow<PayToComponentState> { emit(mock()) }
            whenever(submitHandler.submitFlow) doReturn submitFlow
            val delegate = createPayToDelegate()

            delegate.submitFlow.collectLatest {
                val expectedEvent = GenericEvents.submit(TEST_PAYMENT_METHOD_TYPE)
                analyticsManager.assertLastEventEquals(expectedEvent)
            }
        }

        @Test
        fun `when component state is valid then PaymentMethodDetails should contain checkoutAttemptId`() = runTest {
            analyticsManager.setCheckoutAttemptId(TEST_CHECKOUT_ATTEMPT_ID)

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateInputData { }

                assertEquals(TEST_CHECKOUT_ATTEMPT_ID, expectMostRecentItem().data.paymentMethod?.checkoutAttemptId)
            }
        }

        @Test
        fun `when delegate is cleared then analytics manager is cleared`() {
            delegate.onCleared()

            analyticsManager.assertIsCleared()
        }
    }

    private fun createPayToDelegate(
        configuration: CheckoutConfiguration = createCheckoutConfiguration(),
    ) = DefaultPayToDelegate(
        observerRepository = PaymentObserverRepository(),
        paymentMethod = PaymentMethod(type = TEST_PAYMENT_METHOD_TYPE),
        order = TEST_ORDER,
        componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = Locale.US,
            dropInOverrideParams = null,
            componentSessionParams = null,
            componentConfiguration = configuration.getPayToConfiguration(),
        ),
        analyticsManager = analyticsManager,
        submitHandler = submitHandler,
    )

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: PayToConfiguration.Builder.() -> Unit = {},
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        amount = amount,
    ) {
        payTo(configuration)
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
        private const val TEST_CHECKOUT_ATTEMPT_ID = "TEST_CHECKOUT_ATTEMPT_ID"
        private const val TEST_PAYMENT_METHOD_TYPE = "TEST_PAYMENT_METHOD_TYPE"
    }
}
