/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/7/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui

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
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayAuthorizationData
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayComponentParamsMapper
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayOnFileData
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayOneTimeData
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayOutputData
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.Configuration
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
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
    @Mock private val analyticsRepository: AnalyticsRepository,
    @Mock private val cashAppPayFactory: CashAppPayFactory,
    @Mock private val cashAppPay: CashAppPay,
) {

    private lateinit var delegate: DefaultCashAppPayDelegate

    @BeforeEach
    fun before() {
        whenever(cashAppPayFactory.createSandbox(any())) doReturn cashAppPay
        whenever(cashAppPayFactory.create(any())) doReturn cashAppPay
        delegate = createDefaultCashAppPayDelegate()
    }

    @Nested
    @DisplayName("when delegate is initialized")
    inner class InitializeTest {

        @Test
        fun `then analytics event is sent`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            verify(analyticsRepository).setupAnalytics()
        }

        @Test
        fun `no confirmation is required, then payment should be initiated`() = runTest {
            delegate = createDefaultCashAppPayDelegate(
                getConfigurationBuilder()
                    .setAmount(Amount("USD", 10L))
                    .setShowStorePaymentField(false)
                    .build()
            )
            delegate.initialize(this)

            verify(cashAppPay).createCustomerRequest(paymentActions = any(), redirectUri = anyOrNull())
        }
    }

    @Test
    fun `when input data changes, then component state is created`() = runTest {
        delegate = createDefaultCashAppPayDelegate(
            getConfigurationBuilder()
                .setAmount(Amount("USD", 10L))
                .build()
        )
        val testFlow = delegate.componentStateFlow.test(testScheduler)
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.updateInputData {
            isStorePaymentSelected = true
            authorizationData = CashAppPayAuthorizationData(
                oneTimeData = CashAppPayOneTimeData("grantId", "customerId"),
                onFileData = CashAppPayOnFileData("grantId", "cashTag", "customerId")
            )
        }

        val expected = CashAppPayComponentState(
            data = PaymentComponentData(
                paymentMethod = CashAppPayPaymentMethod(
                    type = null,
                    checkoutAttemptId = null,
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
            isReady = true
        )
        assertEquals(expected, testFlow.latestValue)
    }

    @Nested
    @DisplayName("when submit button is configured to be")
    inner class SubmitButtonVisibilityTest {

        @Test
        fun `hidden, then it should not show`() {
            delegate = createDefaultCashAppPayDelegate(
                configuration = getConfigurationBuilder()
                    .setSubmitButtonVisible(false)
                    .build()
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `visible, then it should show`() {
            delegate = createDefaultCashAppPayDelegate(
                configuration = getConfigurationBuilder()
                    .setSubmitButtonVisible(true)
                    .build()
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
                    getConfigurationBuilder().setAmount(Amount("EUR", 100L)).build()
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
                    getConfigurationBuilder().setAmount(Amount("USD", 100L)).build()
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
                    getConfigurationBuilder().setAmount(Amount("USD", 100L)).build()
                )
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                delegate.onSubmit()

                verify(cashAppPay).createCustomerRequest(
                    listOf(
                        CashAppPayPaymentAction.OneTimeAction(
                            amount = 100,
                            currency = CashAppPayCurrency.USD,
                            scopeId = TEST_SCOPE_ID,
                        )
                    ),
                    TEST_RETURN_URL
                )
            }

        @Test
        fun `the user doesn't want to store and the component is not configured to store, then there is no OnFileAction`() =
            runTest {
                delegate = createDefaultCashAppPayDelegate(
                    getConfigurationBuilder().setAmount(Amount("USD", 100L)).build()
                )
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                delegate.onSubmit()

                verify(cashAppPay).createCustomerRequest(
                    listOf(
                        CashAppPayPaymentAction.OneTimeAction(
                            amount = 100,
                            currency = CashAppPayCurrency.USD,
                            scopeId = TEST_SCOPE_ID,
                        )
                    ),
                    TEST_RETURN_URL
                )
            }

        @Test
        fun `the user wants to store, then the Cash App SDK should be called with an OnFileAction`() =
            runTest {
                delegate = createDefaultCashAppPayDelegate(
                    getConfigurationBuilder()
                        .setAmount(Amount("USD", 0L))
                        .setShowStorePaymentField(true)
                        .build()
                )
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
                delegate.updateInputData { isStorePaymentSelected = true }

                delegate.onSubmit()

                verify(cashAppPay).createCustomerRequest(
                    listOf(
                        CashAppPayPaymentAction.OnFileAction(scopeId = TEST_SCOPE_ID),
                    ),
                    TEST_RETURN_URL
                )
            }

        @Test
        fun `the component is configured to store, then the Cash App SDK should be called with an OnFileAction`() =
            runTest {
                delegate = createDefaultCashAppPayDelegate(
                    getConfigurationBuilder()
                        .setAmount(Amount("USD", 0L))
                        .setShowStorePaymentField(false)
                        .setStorePaymentMethod(true)
                        .build()
                )
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                verify(cashAppPay).createCustomerRequest(
                    listOf(
                        CashAppPayPaymentAction.OnFileAction(scopeId = TEST_SCOPE_ID),
                    ),
                    TEST_RETURN_URL
                )
            }

        @Test
        fun `the component doesn't require confirmation, then the Cash App SDK should not be called`() =
            runTest {
                delegate = createDefaultCashAppPayDelegate(
                    getConfigurationBuilder()
                        .setAmount(Amount("USD", 0L))
                        .setShowStorePaymentField(false)
                        .setStorePaymentMethod(true)
                        .build()
                )
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                delegate.onSubmit()

                // Called once on initialization, but shouldn't be called by onSubmit
                verify(cashAppPay, times(1)).createCustomerRequest(paymentActions = any(), redirectUri = anyOrNull())
            }
    }

    @ParameterizedTest
    @MethodSource("amountSource")
    fun `when updating component state, then amount is propagated in component state if set`(
        configurationValue: Amount?,
        expectedComponentStateValue: Amount?,
    ) = runTest {
        if (configurationValue != null) {
            val configuration = getConfigurationBuilder()
                .setAmount(configurationValue)
                .build()
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
                createGrant(GrantType.EXTENDED)
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
                createGrant(GrantType.EXTENDED)
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
        fun `when component state is valid then PaymentMethodDetails should contain checkoutAttemptId`() = runTest {
            whenever(analyticsRepository.getCheckoutAttemptId()) doReturn TEST_CHECKOUT_ATTEMPT_ID

            val testFlow = delegate.componentStateFlow.test(testScheduler)
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.updateInputData {
                authorizationData = CashAppPayAuthorizationData(
                    oneTimeData = CashAppPayOneTimeData("grantId", "customerId"),
                    onFileData = CashAppPayOnFileData("grantId", "cashTag", "customerId")
                )
            }

            assertEquals(TEST_CHECKOUT_ATTEMPT_ID, testFlow.latestValue.data.paymentMethod?.checkoutAttemptId)
        }
    }

    private fun createDefaultCashAppPayDelegate(
        configuration: CashAppPayConfiguration = getConfigurationBuilder().build()
    ) = DefaultCashAppPayDelegate(
        submitHandler = submitHandler,
        analyticsRepository = analyticsRepository,
        observerRepository = PaymentObserverRepository(),
        paymentMethod = getPaymentMethod(),
        order = TEST_ORDER,
        componentParams = CashAppPayComponentParamsMapper(null, null).mapToParams(
            configuration = configuration,
            sessionParams = null,
            paymentMethod = getPaymentMethod(),
        ),
        cashAppPayFactory = cashAppPayFactory,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    private fun getConfigurationBuilder() = CashAppPayConfiguration.Builder(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = "test_qwertyuiopasdfghjklzxcvbnmqwerty",
    )
        .setReturnUrl(TEST_RETURN_URL)

    private fun getPaymentMethod() = PaymentMethod(
        configuration = Configuration(
            clientId = "clientId",
            scopeId = TEST_SCOPE_ID,
        ),
    )

    companion object {
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
        private const val TEST_RETURN_URL = "testReturnUrl"
        private const val TEST_SCOPE_ID = "testScopeId"
        private const val TEST_CHECKOUT_ATTEMPT_ID = "TEST_CHECKOUT_ATTEMPT_ID"

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
