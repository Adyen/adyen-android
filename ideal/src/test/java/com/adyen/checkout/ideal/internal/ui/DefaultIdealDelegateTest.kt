package com.adyen.checkout.ideal.internal.ui

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParamsMapper
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.extensions.test
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
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, LoggingExtension::class)
internal class DefaultIdealDelegateTest {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var delegate: DefaultIdealDelegate

    @BeforeEach
    fun before() {
        analyticsManager = TestAnalyticsManager()
        delegate = createIdealDelegate()
    }

    @Test
    fun `when subscribed, then component state flow should propagate a valid state`() = runTest {
        val componentStateFlow = delegate.componentStateFlow.test(testScheduler)

        with(componentStateFlow.latestValue) {
            assertEquals(TEST_PAYMENT_METHOD_TYPE, data.paymentMethod?.type)
            assertEquals(TEST_ORDER, data.order)
            assertTrue(isInputValid)
            assertTrue(isValid)
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
            delegate = createIdealDelegate(configuration = configuration)
        }
        val componentStateFlow = delegate.componentStateFlow.test(testScheduler)

        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        assertEquals(expectedComponentStateValue, componentStateFlow.latestValue.data.amount)
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
            analyticsManager.assertHasEventEquals(expectedEvent)
        }

        @Test
        fun `when delegate is initialized, then submit event is tracked`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val expectedEvent = GenericEvents.submit(TEST_PAYMENT_METHOD_TYPE)
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when component state is valid then PaymentMethodDetails should contain checkoutAttemptId`() = runTest {
            analyticsManager.setCheckoutAttemptId(TEST_CHECKOUT_ATTEMPT_ID)

            delegate = createIdealDelegate()
            val componentStateFlow = delegate.componentStateFlow.test(testScheduler)

            assertEquals(TEST_CHECKOUT_ATTEMPT_ID, componentStateFlow.latestValue.data.paymentMethod?.checkoutAttemptId)
        }

        @Test
        fun `when delegate is cleared then analytics manager is cleared`() {
            delegate.onCleared()

            analyticsManager.assertIsCleared()
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

    private fun createIdealDelegate(
        configuration: CheckoutConfiguration = createCheckoutConfiguration(),
    ): DefaultIdealDelegate {
        return DefaultIdealDelegate(
            observerRepository = PaymentObserverRepository(),
            paymentMethod = PaymentMethod(type = TEST_PAYMENT_METHOD_TYPE),
            order = TEST_ORDER,
            componentParams = GenericComponentParamsMapper(CommonComponentParamsMapper())
                .mapToParams(configuration, Locale.US, null, null),
            analyticsManager = analyticsManager,
        )
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_PAYMENT_METHOD_TYPE = "TEST_PAYMENT_METHOD_TYPE"
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
