/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/7/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui

import android.app.Application
import app.cash.paykit.core.CashAppPay
import app.cash.paykit.core.CashAppPayFactory
import app.cash.paykit.core.CashAppPayState
import app.cash.paykit.core.models.common.Action
import app.cash.paykit.core.models.pii.PiiString
import app.cash.paykit.core.models.response.CustomerProfile
import app.cash.paykit.core.models.response.CustomerResponseData
import app.cash.paykit.core.models.response.Grant
import app.cash.paykit.core.models.response.GrantType
import app.cash.paykit.core.models.sdk.CashAppPayCurrency
import app.cash.paykit.core.models.sdk.CashAppPayPaymentAction
import com.adyen.checkout.cashapppay.CashAppPayComponentState
import com.adyen.checkout.cashapppay.CashAppPayConfiguration
import com.adyen.checkout.cashapppay.cashAppPay
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayAuthorizationData
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayComponentParamsMapper
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayOnFileData
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayOneTimeData
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayOutputData
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.Configuration
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.ErrorEvent
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.paymentmethod.CashAppPayPaymentMethod
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.test
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class DefaultCashAppPayDelegateTest(
    @Mock private val submitHandler: SubmitHandler<CashAppPayComponentState>,
    @Mock private val cashAppPayFactory: CashAppPayFactory,
    @Mock private val cashAppPay: CashAppPay,
) {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var delegate: DefaultCashAppPayDelegate

    @BeforeEach
    fun before() {
        analyticsManager = TestAnalyticsManager()
        whenever(cashAppPayFactory.createSandbox(any())) doReturn cashAppPay
        whenever(cashAppPayFactory.create(any())) doReturn cashAppPay
        delegate = createDefaultCashAppPayDelegate()
    }

    @Nested
    @DisplayName("when delegate is initialized")
    inner class InitializeTest {

        @Test
        fun `no confirmation is required, then payment should be initiated`() = runTest {
            delegate = createDefaultCashAppPayDelegate(
                createCheckoutConfiguration(Amount("USD", 10L)) {
                    setShowStorePaymentField(false)
                },
            )
            delegate.initialize(this)

            verify(cashAppPay).createCustomerRequest(
                paymentActions = any(),
                redirectUri = anyOrNull(),
                referenceId = anyOrNull(),
            )
        }
    }

    @Test
    fun `when input data changes, then component state is created`() = runTest {
        delegate = createDefaultCashAppPayDelegate(
            createCheckoutConfiguration(Amount("USD", 10L)),
        )
        val testFlow = delegate.componentStateFlow.test(testScheduler)
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.updateInputData {
            isStorePaymentSelected = true
            authorizationData = CashAppPayAuthorizationData(
                oneTimeData = CashAppPayOneTimeData("grantId", "customerId"),
                onFileData = CashAppPayOnFileData("grantId", "cashTag", "customerId"),
            )
        }

        val expected = CashAppPayComponentState(
            data = PaymentComponentData(
                paymentMethod = CashAppPayPaymentMethod(
                    type = TEST_PAYMENT_METHOD_TYPE,
                    checkoutAttemptId = TestAnalyticsManager.CHECKOUT_ATTEMPT_ID_NOT_FETCHED,
                    grantId = "grantId",
                    onFileGrantId = "grantId",
                    customerId = "customerId",
                    cashtag = "cashTag",
                    storedPaymentMethodId = null,
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
    @DisplayName("when submit button is configured to be")
    inner class SubmitButtonVisibilityTest {

        @Test
        fun `hidden, then it should not show`() {
            delegate = createDefaultCashAppPayDelegate(
                configuration = createCheckoutConfiguration {
                    setSubmitButtonVisible(false)
                },
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `visible, then it should show`() {
            delegate = createDefaultCashAppPayDelegate(
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
        fun `there are no actions, then an exception should be propagated`() =
            runTest {
                val testFlow = delegate.exceptionFlow.test(testScheduler)
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                delegate.onSubmit()

                assertTrue(testFlow.latestValue is ComponentException)
                assertEquals(1, testFlow.values.size)
            }

        @Test
        fun `the currency is not supported, then an exception should be propagated`() =
            runTest {
                delegate = createDefaultCashAppPayDelegate(
                    createCheckoutConfiguration(Amount("EUR", 100L)),
                )
                val testFlow = delegate.exceptionFlow.test(testScheduler)
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                delegate.onSubmit()

                assertEquals(2, testFlow.values.size)
                testFlow.values.forEach {
                    assertTrue(it is ComponentException)
                }
            }

        @Test
        fun `there is any valid action, then the loading view should be shown`() =
            runTest {
                delegate = createDefaultCashAppPayDelegate(
                    createCheckoutConfiguration(Amount("USD", 100L)),
                )
                val testFlow = delegate.viewFlow.test(testScheduler)
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                delegate.onSubmit()

                assertTrue(testFlow.latestValue is PaymentInProgressViewType)
            }

        @Test
        fun `there is an OneTimeAction, then the Cash App SDK should be called with it`() =
            runTest {
                delegate = createDefaultCashAppPayDelegate(
                    createCheckoutConfiguration(Amount("USD", 100L)),
                )
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                delegate.onSubmit()

                verify(cashAppPay).createCustomerRequest(
                    listOf(
                        CashAppPayPaymentAction.OneTimeAction(
                            amount = 100,
                            currency = CashAppPayCurrency.USD,
                            scopeId = TEST_SCOPE_ID,
                        ),
                    ),
                    TEST_RETURN_URL,
                )
            }

        @Test
        fun `the user doesn't want to store and the component is not configured to store, then there is no OnFileAction`() =
            runTest {
                delegate = createDefaultCashAppPayDelegate(
                    createCheckoutConfiguration(Amount("USD", 100L)),
                )
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                delegate.onSubmit()

                verify(cashAppPay).createCustomerRequest(
                    listOf(
                        CashAppPayPaymentAction.OneTimeAction(
                            amount = 100,
                            currency = CashAppPayCurrency.USD,
                            scopeId = TEST_SCOPE_ID,
                        ),
                    ),
                    TEST_RETURN_URL,
                )
            }

        @Test
        fun `the user wants to store, then the Cash App SDK should be called with an OnFileAction`() =
            runTest {
                delegate = createDefaultCashAppPayDelegate(
                    createCheckoutConfiguration(Amount("USD", 0L)) {
                        setShowStorePaymentField(true)
                    },
                )
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
                delegate.updateInputData { isStorePaymentSelected = true }

                delegate.onSubmit()

                verify(cashAppPay).createCustomerRequest(
                    listOf(
                        CashAppPayPaymentAction.OnFileAction(scopeId = TEST_SCOPE_ID),
                    ),
                    TEST_RETURN_URL,
                )
            }

        @Test
        fun `the component is configured to store, then the Cash App SDK should be called with an OnFileAction`() =
            runTest {
                delegate = createDefaultCashAppPayDelegate(
                    createCheckoutConfiguration(Amount("USD", 0L)) {
                        setShowStorePaymentField(false)
                        setStorePaymentMethod(true)
                    },
                )
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                verify(cashAppPay).createCustomerRequest(
                    listOf(
                        CashAppPayPaymentAction.OnFileAction(scopeId = TEST_SCOPE_ID),
                    ),
                    TEST_RETURN_URL,
                )
            }

        @Test
        fun `the component doesn't require confirmation, then the Cash App SDK should not be called`() =
            runTest {
                delegate = createDefaultCashAppPayDelegate(
                    createCheckoutConfiguration(Amount("USD", 0L)) {
                        setShowStorePaymentField(false)
                        setStorePaymentMethod(true)
                    },
                )
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                delegate.onSubmit()

                // Called once on initialization, but shouldn't be called by onSubmit
                verify(cashAppPay, times(1)).createCustomerRequest(
                    paymentActions = any(),
                    redirectUri = anyOrNull(),
                    referenceId = anyOrNull(),
                )
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
            delegate = createDefaultCashAppPayDelegate(configuration = configuration)
        }
        val testFlow = delegate.componentStateFlow.test(testScheduler)
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.updateComponentState(CashAppPayOutputData(false, null))

        assertEquals(expectedComponentStateValue, testFlow.latestValue.data.amount)
    }

    @Nested
    @DisplayName("when cash app pay state changes and state is")
    inner class CashAppPayStateChangeTest {

        @Test
        fun `ready to authorize, then the cash app SDK should be used to authorize`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.cashAppPayStateDidChange(CashAppPayState.ReadyToAuthorize(mock()))

            verify(cashAppPay).authorizeCustomerRequest()
        }

        @Test
        fun `approved, then component state is updated`() = runTest {
            val testFlow = delegate.componentStateFlow.test(testScheduler)
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            assertFalse(testFlow.latestValue.isValid)

            // We have to mock this class, because we get an error when using it normally
            val mockResponse = mock<CustomerResponseData>()
            whenever(mockResponse.grants) doReturn listOf(
                createGrant(GrantType.ONE_TIME),
                createGrant(GrantType.EXTENDED),
            )
            whenever(mockResponse.customerProfile) doReturn CustomerProfile("customerId", PiiString("cashTag"))
            delegate.cashAppPayStateDidChange(CashAppPayState.Approved(mockResponse))

            val actual = testFlow.latestValue
            assertTrue(actual.isValid)
            assertEquals("id", actual.data.paymentMethod?.grantId)
            assertEquals("customerId", actual.data.paymentMethod?.customerId)
            assertEquals("id", actual.data.paymentMethod?.onFileGrantId)
            assertEquals("cashTag", actual.data.paymentMethod?.cashtag)
        }

        @Test
        fun `approved, then submit handler is called`() = runTest {
            val testFlow = delegate.componentStateFlow.test(testScheduler)
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            assertFalse(testFlow.latestValue.isValid)

            // We have to mock this class, because we get an error when using it normally
            val mockResponse = mock<CustomerResponseData>()
            whenever(mockResponse.grants) doReturn listOf(
                createGrant(GrantType.ONE_TIME),
                createGrant(GrantType.EXTENDED),
            )
            whenever(mockResponse.customerProfile) doReturn CustomerProfile("customerId", PiiString("cashTag"))
            delegate.cashAppPayStateDidChange(CashAppPayState.Approved(mockResponse))

            verify(submitHandler).onSubmit(testFlow.latestValue)
        }

        @Test
        fun `declined, then an error is propagated`() = runTest {
            val testFlow = delegate.exceptionFlow.test(testScheduler)
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.cashAppPayStateDidChange(CashAppPayState.Declined)

            assertTrue(testFlow.latestValue is ComponentException)
        }

        @Test
        fun `exception, then an error is propagated`() = runTest {
            val testFlow = delegate.exceptionFlow.test(testScheduler)
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val exception = RuntimeException("Stub!")

            delegate.cashAppPayStateDidChange(CashAppPayState.CashAppPayExceptionState(exception))

            assertEquals(exception, (testFlow.latestValue as ComponentException).cause)
        }

        private fun createGrant(type: GrantType) = Grant(
            id = "id",
            status = "",
            type = type,
            action = Action(null, null, "", ""),
            channel = "",
            customerId = "",
            updatedAt = "",
            createdAt = "",
            expiresAt = "",
        )
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
        fun `when delegate is initialized, then submit event is not tracked`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val expectedEvent = GenericEvents.submit(TEST_PAYMENT_METHOD_TYPE)
            analyticsManager.assertLastEventNotEquals(expectedEvent)
        }

        @Test
        fun `when delegate is initialized and confirmation is not required, then submit event is tracked`() {
            delegate = createDefaultCashAppPayDelegate(
                createCheckoutConfiguration(Amount("USD", 10L)) {
                    setShowStorePaymentField(false)
                },
            )

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val expectedEvent = GenericEvents.submit(TEST_PAYMENT_METHOD_TYPE)
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when component state is valid then PaymentMethodDetails should contain checkoutAttemptId`() = runTest {
            analyticsManager.setCheckoutAttemptId(TEST_CHECKOUT_ATTEMPT_ID)

            val testFlow = delegate.componentStateFlow.test(testScheduler)
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.updateInputData {
                authorizationData = CashAppPayAuthorizationData(
                    oneTimeData = CashAppPayOneTimeData("grantId", "customerId"),
                    onFileData = CashAppPayOnFileData("grantId", "cashTag", "customerId"),
                )
            }

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
            delegate = createDefaultCashAppPayDelegate(
                createCheckoutConfiguration(Amount("USD", 10L)) {
                    setShowStorePaymentField(false)
                },
            )

            delegate.onSubmit()

            val expectedEvent = GenericEvents.submit(TEST_PAYMENT_METHOD_TYPE)
            analyticsManager.assertLastEventNotEquals(expectedEvent)
        }

        @Test
        fun `when state is exception, then an error is tracked`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val exception = RuntimeException("Stub!")

            delegate.cashAppPayStateDidChange(CashAppPayState.CashAppPayExceptionState(exception))

            val expectedEvent = GenericEvents.error(
                component = TEST_PAYMENT_METHOD_TYPE,
                event = ErrorEvent.THIRD_PARTY
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when delegate is cleared then analytics manager is cleared`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.onCleared()

            analyticsManager.assertIsCleared()
        }
    }

    private fun createDefaultCashAppPayDelegate(
        configuration: CheckoutConfiguration = createCheckoutConfiguration(),
    ) = DefaultCashAppPayDelegate(
        submitHandler = submitHandler,
        analyticsManager = analyticsManager,
        observerRepository = PaymentObserverRepository(),
        paymentMethod = getPaymentMethod(),
        order = TEST_ORDER,
        componentParams = CashAppPayComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = Locale.US,
            dropInOverrideParams = null,
            componentSessionParams = null,
            paymentMethod = getPaymentMethod(),
            context = Application(),
        ),
        cashAppPayFactory = cashAppPayFactory,
        coroutineDispatcher = UnconfinedTestDispatcher(),
    )

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: CashAppPayConfiguration.Builder.() -> Unit = {},
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = "test_qwertyuiopasdfghjklzxcvbnmqwerty",
        amount = amount,
    ) {
        cashAppPay {
            setReturnUrl(TEST_RETURN_URL)
            apply(configuration)
        }
    }

    private fun getPaymentMethod() = PaymentMethod(
        configuration = Configuration(
            clientId = "clientId",
            scopeId = TEST_SCOPE_ID,
        ),
        type = TEST_PAYMENT_METHOD_TYPE,
    )

    companion object {
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
        private const val TEST_RETURN_URL = "testReturnUrl"
        private const val TEST_SCOPE_ID = "testScopeId"
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
