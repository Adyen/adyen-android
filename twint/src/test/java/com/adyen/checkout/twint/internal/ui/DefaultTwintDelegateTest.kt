package com.adyen.checkout.twint.internal.ui

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.Environment
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.test
import com.adyen.checkout.twint.TwintComponentState
import com.adyen.checkout.twint.TwintConfiguration
import com.adyen.checkout.twint.internal.ui.model.TwintComponentParamsMapper
import com.adyen.checkout.twint.twint
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class DefaultTwintDelegateTest(
    @Mock private val submitHandler: SubmitHandler<TwintComponentState>,
) {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var delegate: DefaultTwintDelegate

    @BeforeEach
    fun before() {
        analyticsManager = TestAnalyticsManager()
        delegate = createDefaultTwintDelegate()
    }

    @Nested
    @DisplayName("when delegate is initialized")
    inner class InitializeTest {

        @Test
        fun `no confirmation is required, then payment should be initiated`() = runTest {
            delegate = createDefaultTwintDelegate(
                createCheckoutConfiguration(Amount("USD", 10L)) {
                    setShowStorePaymentField(false)
                },
            )
            delegate.initialize(this)

            verify(submitHandler).onSubmit(any())
        }
    }

    @Nested
    @DisplayName("when submit button is configured to be")
    inner class SubmitButtonVisibilityTest {

        @Test
        fun `hidden, then it should not show`() {
            delegate = createDefaultTwintDelegate(
                configuration = createCheckoutConfiguration {
                    setSubmitButtonVisible(false)
                },
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `visible, then it should show`() {
            delegate = createDefaultTwintDelegate(
                configuration = createCheckoutConfiguration {
                    setSubmitButtonVisible(true)
                },
            )

            assertTrue(delegate.shouldShowSubmitButton())
        }
    }

    @Nested
    inner class SubmitButtonEnableTest {

        @Test
        fun `when shouldEnableSubmitButton is called, then true is returned`() {
            assertTrue(delegate.shouldEnableSubmitButton())
        }
    }

    @Nested
    inner class SubmitHandlerTest {

        @Test
        fun `when delegate is initialized, then submit handler event is initialized`() = runTest {
            val coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
            delegate.initialize(coroutineScope)
            verify(submitHandler).initialize(coroutineScope, delegate.componentStateFlow)
        }

        @Test
        fun `when delegate setInteractionBlocked is called, then submit handler setInteractionBlocked is called`() =
            runTest {
                delegate.setInteractionBlocked(true)
                verify(submitHandler).setInteractionBlocked(true)
            }
    }

    @Nested
    @DisplayName("when onSubmit is called and")
    inner class OnSubmitTest {

        @Test
        fun `the component doesn't require confirmation, then the submit handler should not be called`() = runTest {
            delegate = createDefaultTwintDelegate(
                createCheckoutConfiguration(Amount("USD", 0L)) {
                    setShowStorePaymentField(false)
                    setStorePaymentMethod(true)
                },
            )
            delegate.initialize(this)

            delegate.onSubmit()

            // Called once on initialization, but shouldn't be called by onSubmit
            verify(submitHandler, times(1)).onSubmit(any())
        }
    }

    @Nested
    inner class AnalyticsTest {

        @Test
        fun `when delegate is initialized, then analytics manager is initialized`() {
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
        fun `when delegate is initialized, then submit event is not tracked`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val expectedEvent = GenericEvents.submit(TEST_PAYMENT_METHOD_TYPE)
            analyticsManager.assertLastEventNotEquals(expectedEvent)
        }

        @Test
        fun `when delegate is initialized and confirmation is not required, then submit event is tracked`() {
            delegate = createDefaultTwintDelegate(
                createCheckoutConfiguration(Amount("USD", 10L)) {
                    setShowStorePaymentField(false)
                },
            )

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val expectedEvent = GenericEvents.submit(TEST_PAYMENT_METHOD_TYPE)
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when component state is valid, then payment method should contain checkoutAttemptId`() = runTest {
            analyticsManager.setCheckoutAttemptId(TEST_CHECKOUT_ATTEMPT_ID)
            delegate = createDefaultTwintDelegate()

            val testFlow = delegate.componentStateFlow.test(testScheduler)
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            assertEquals(TEST_CHECKOUT_ATTEMPT_ID, testFlow.latestValue.data.paymentMethod?.checkoutAttemptId)
        }

        @Test
        fun `when onSubmit is called, then submit event is tracked`() {
            delegate.onSubmit()

            val expectedEvent = GenericEvents.submit(TEST_PAYMENT_METHOD_TYPE)
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when onSubmit is called and confirmation is not required, then submit event is not tracked`() {
            delegate = createDefaultTwintDelegate(
                createCheckoutConfiguration(Amount("USD", 10L)) {
                    setShowStorePaymentField(false)
                },
            )

            delegate.onSubmit()

            val expectedEvent = GenericEvents.submit(TEST_PAYMENT_METHOD_TYPE)
            analyticsManager.assertLastEventNotEquals(expectedEvent)
        }

        @Test
        fun `when delegate is cleared then analytics manager is cleared`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.onCleared()

            analyticsManager.assertIsCleared()
        }
    }

    private fun createDefaultTwintDelegate(
        configuration: CheckoutConfiguration = createCheckoutConfiguration(),
    ) = DefaultTwintDelegate(
        submitHandler = submitHandler,
        analyticsManager = analyticsManager,
        observerRepository = PaymentObserverRepository(),
        paymentMethod = PaymentMethod(type = TEST_PAYMENT_METHOD_TYPE),
        order = TEST_ORDER,
        componentParams = TwintComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = Locale.US,
            dropInOverrideParams = null,
            componentSessionParams = null,
        ),
    )

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: TwintConfiguration.Builder.() -> Unit = {},
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = "test_qwertyuiopasdfghjklzxcvbnmqwerty",
        amount = amount,
    ) {
        twint(configuration)
    }

    companion object {
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
        private const val TEST_CHECKOUT_ATTEMPT_ID = "TEST_CHECKOUT_ATTEMPT_ID"
        private const val TEST_PAYMENT_METHOD_TYPE = "TEST_PAYMENT_METHOD_TYPE"
    }
}
