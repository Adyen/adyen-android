package com.adyen.checkout.twint.internal.ui

import com.adyen.checkout.components.core.ActionHandlingMethod
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.paymentmethod.TwintPaymentMethod
import com.adyen.checkout.core.Environment
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.test
import com.adyen.checkout.twint.TwintComponentState
import com.adyen.checkout.twint.TwintConfiguration
import com.adyen.checkout.twint.internal.ui.model.TwintComponentParamsMapper
import com.adyen.checkout.twint.internal.ui.model.TwintOutputData
import com.adyen.checkout.twint.twint
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
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
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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

    @Test
    fun `when input data changes, then component state is created`() = runTest {
        delegate = createDefaultTwintDelegate(
            createCheckoutConfiguration(Amount("USD", 10L)),
        )
        val testFlow = delegate.componentStateFlow.test(testScheduler)
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.updateInputData {
            isStorePaymentSelected = true
        }

        val expected = TwintComponentState(
            data = PaymentComponentData(
                paymentMethod = TwintPaymentMethod(
                    type = TEST_PAYMENT_METHOD_TYPE,
                    checkoutAttemptId = TestAnalyticsManager.CHECKOUT_ATTEMPT_ID_NOT_FETCHED,
                    subtype = "sdk",
                ),
                order = TEST_ORDER,
                amount = Amount("USD", 10L),
                storePaymentMethod = true,
            ),
            isInputValid = true,
            isReady = true,
        )
        assertEquals(expected, testFlow.latestValue)
    }

    @Nested
    @DisplayName("when actions should be handled with ")
    inner class ActionHandlingMethodTest {

        @Test
        fun `SDK, then sub type is set in payment method`() = runTest {
            val configuration = createCheckoutConfiguration {
                setActionHandlingMethod(ActionHandlingMethod.PREFER_NATIVE)
            }
            delegate = createDefaultTwintDelegate(configuration)
            val componentStateFlow = delegate.componentStateFlow.test(testScheduler)

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val actual = componentStateFlow.latestValue.data.paymentMethod?.subtype
            assertEquals(DefaultTwintDelegate.SDK_SUBTYPE, actual)
        }

        @Test
        fun `WEB, then sub type is not set in payment method`() = runTest {
            val configuration = createCheckoutConfiguration {
                setActionHandlingMethod(ActionHandlingMethod.PREFER_WEB)
            }
            delegate = createDefaultTwintDelegate(configuration)
            val componentStateFlow = delegate.componentStateFlow.test(testScheduler)

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            assertNull(componentStateFlow.latestValue.data.paymentMethod?.subtype)
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
                },
            )
            delegate.initialize(this)

            delegate.onSubmit()

            // Called once on initialization, but shouldn't be called by onSubmit
            verify(submitHandler, times(1)).onSubmit(any())
        }

        @Test
        fun `the component does require confirmation, then the submit handler should be called`() = runTest {
            delegate = createDefaultTwintDelegate(
                createCheckoutConfiguration(Amount("USD", 0L)) {
                    setShowStorePaymentField(true)
                },
            )
            delegate.initialize(this)

            delegate.onSubmit()

            // Called once on initialization, but shouldn't be called by onSubmit
            verify(submitHandler, times(1)).onSubmit(any())
        }

        @Test
        fun `the user doesn't want to store, then we don't store the pm`() =
            runTest {
                delegate = createDefaultTwintDelegate(
                    createCheckoutConfiguration(Amount("USD", 100L)) {
                        setShowStorePaymentField(true)
                    },
                )
                val componentStateFlow = delegate.componentStateFlow.test(testScheduler)
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
                delegate.updateInputData { isStorePaymentSelected = false }

                delegate.onSubmit()

                assertEquals(false, componentStateFlow.latestValue.data.storePaymentMethod)
            }

        @Test
        fun `the user wants to store, then we store the pm`() =
            runTest {
                delegate = createDefaultTwintDelegate(
                    createCheckoutConfiguration(Amount("USD", 0L)) {
                        setShowStorePaymentField(true)
                    },
                )
                val componentStateFlow = delegate.componentStateFlow.test(testScheduler)
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
                delegate.updateInputData { isStorePaymentSelected = true }

                delegate.onSubmit()

                assertEquals(true, componentStateFlow.latestValue.data.storePaymentMethod)
            }

        @Test
        fun `store checkbox is not shown, then we leave it up to the api`() =
            runTest {
                delegate = createDefaultTwintDelegate(
                    createCheckoutConfiguration(Amount("USD", 0L)) {
                        setShowStorePaymentField(false)
                    },
                )
                val componentStateFlow = delegate.componentStateFlow.test(testScheduler)
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
                delegate.updateInputData { isStorePaymentSelected = true }

                delegate.onSubmit()

                assertNull(componentStateFlow.latestValue.data.storePaymentMethod)
            }
    }

    @ParameterizedTest
    @MethodSource("amountSource")
    fun `when updating component state, then amount is propagated in component state if set`(
        configurationValue: Amount?,
        expectedComponentStateValue: Amount?,
    ) = runTest {
        if (configurationValue != null) {
            val configuration = createCheckoutConfiguration(configurationValue)
            delegate = createDefaultTwintDelegate(configuration = configuration)
        }
        val testFlow = delegate.componentStateFlow.test(testScheduler)
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.updateComponentState(TwintOutputData(false))

        assertEquals(expectedComponentStateValue, testFlow.latestValue.data.amount)
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
        fun `when component state is valid, then payment method should contain checkoutAttemptId`() = runTest {
            analyticsManager.setCheckoutAttemptId(TEST_CHECKOUT_ATTEMPT_ID)
            delegate = createDefaultTwintDelegate()

            val testFlow = delegate.componentStateFlow.test(testScheduler)
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            assertEquals(TEST_CHECKOUT_ATTEMPT_ID, testFlow.latestValue.data.paymentMethod?.checkoutAttemptId)
        }

        @Test
        fun `when submitFlow emits an event, then submit event is tracked`() = runTest {
            val submitFlow = flow<TwintComponentState> { emit(mock()) }
            whenever(submitHandler.submitFlow) doReturn submitFlow
            val delegate = createDefaultTwintDelegate()

            delegate.submitFlow.collectLatest {
                val expectedEvent = GenericEvents.submit(TEST_PAYMENT_METHOD_TYPE)
                analyticsManager.assertLastEventEquals(expectedEvent)
            }
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
