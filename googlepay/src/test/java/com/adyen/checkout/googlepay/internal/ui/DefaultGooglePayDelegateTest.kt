/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/7/2022.
 */

package com.adyen.checkout.googlepay.internal.ui

import app.cash.turbine.test
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.Configuration
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.googlepay.googlePay
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParamsMapper
import com.adyen.checkout.googlepay.internal.util.GooglePayUtils
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.extensions.test
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.contract.ApiTaskResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
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
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, LoggingExtension::class)
internal class DefaultGooglePayDelegateTest(
    @Mock private val paymentsClient: PaymentsClient,
) {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var delegate: DefaultGooglePayDelegate

    @BeforeEach
    fun beforeEach() {
        analyticsManager = TestAnalyticsManager()
        delegate = createGooglePayDelegate()
    }

    @Test
    fun `when delegate is initialized, then state is not valid`() = runTest {
        delegate.componentStateFlow.test {
            with(awaitItem()) {
                assertNull(data.paymentMethod)
                assertFalse(isInputValid)
                assertTrue(isReady)
                assertNull(paymentData)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when payment data is null, then state is not valid`() = runTest {
        delegate.componentStateFlow.test {
            delegate.updateComponentState(null)

            with(awaitItem()) {
                assertNull(data.paymentMethod)
                assertFalse(isInputValid)
                assertTrue(isReady)
                assertNull(paymentData)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when creating component state with valid payment data, then the state is propagated`() = runTest {
        delegate.componentStateFlow.test {
            skipItems(1)

            val paymentData = TEST_PAYMENT_DATA

            delegate.updateComponentState(paymentData)

            val componentState = awaitItem()

            with(componentState) {
                assertTrue(isInputValid)
                assertTrue(isReady)
                assertEquals(paymentData, paymentData)
            }

            val paymentComponentData = componentState.data
            with(paymentComponentData) {
                assertEquals(TEST_ORDER, order)
            }

            val expectedPaymentMethod = GooglePayUtils.createGooglePayPaymentMethod(
                paymentData = paymentData,
                paymentMethodType = TEST_PAYMENT_METHOD_TYPE,
                checkoutAttemptId = TestAnalyticsManager.CHECKOUT_ATTEMPT_ID_NOT_FETCHED,
            )
            assertEquals(expectedPaymentMethod, paymentComponentData.paymentMethod)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when onSubmit is called, then event is emitted to start Google Pay`() = runTest {
        val task = Tasks.forResult(TEST_PAYMENT_DATA)
        whenever(paymentsClient.loadPaymentData(any())) doReturn task
        val payEventFlow = delegate.payEventFlow.test(testScheduler)
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        delegate.onSubmit()

        assertEquals(task, payEventFlow.latestValue)
    }

    @ParameterizedTest
    @MethodSource("amountSource")
    fun `when input data is valid then amount is propagated in component state if set`(
        configurationValue: Amount?,
        expectedComponentStateValue: Amount?,
    ) = runTest {
        if (configurationValue != null) {
            val configuration = createCheckoutConfiguration(configurationValue)
            delegate = createGooglePayDelegate(configuration = configuration)
        }
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        delegate.componentStateFlow.test {
            delegate.updateComponentState(TEST_PAYMENT_DATA)
            assertEquals(expectedComponentStateValue, expectMostRecentItem().data.amount)
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
        fun `when component state updates amd the data is valid, then submit event is tracked`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.updateComponentState(TEST_PAYMENT_DATA)

            val expectedEvent = GenericEvents.submit(TEST_PAYMENT_METHOD_TYPE)
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when component state is valid then PaymentMethodDetails should contain checkoutAttemptId`() = runTest {
            analyticsManager.setCheckoutAttemptId(TEST_CHECKOUT_ATTEMPT_ID)

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateComponentState(TEST_PAYMENT_DATA)

                assertEquals(TEST_CHECKOUT_ATTEMPT_ID, expectMostRecentItem().data.paymentMethod?.checkoutAttemptId)
            }
        }

        @Test
        fun `when delegate is cleared then analytics manager is cleared`() {
            delegate.onCleared()

            analyticsManager.assertIsCleared()
        }
    }

    @ParameterizedTest
    @MethodSource("paymentResultSource")
    fun `when handling payment result, then success or error is emitted`(
        result: ApiTaskResult<PaymentData>,
        isSuccess: Boolean,
    ) = runTest {
        val componentStateFlow = delegate.componentStateFlow.test(testScheduler)
        val exceptionFlow = delegate.exceptionFlow.test(testScheduler)

        delegate.handlePaymentResult(result)

        if (isSuccess) {
            assertEquals(result.result, componentStateFlow.latestValue.paymentData)
        } else {
            assertInstanceOf(ComponentException::class.java, exceptionFlow.latestValue)
        }
    }

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: GooglePayConfiguration.Builder.() -> Unit = {}
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = "test_qwertyuiopasdfghjklzxcvbnmqwerty",
        amount = amount,
    ) {
        googlePay(configuration)
    }

    private fun createGooglePayDelegate(
        configuration: CheckoutConfiguration = createCheckoutConfiguration(),
        paymentMethod: PaymentMethod = PaymentMethod(
            configuration = Configuration(gatewayMerchantId = "TEST_GATEWAY_MERCHANT_ID"),
        ),
    ): DefaultGooglePayDelegate {
        return DefaultGooglePayDelegate(
            observerRepository = PaymentObserverRepository(),
            paymentMethod = PaymentMethod(type = TEST_PAYMENT_METHOD_TYPE),
            order = TEST_ORDER,
            componentParams = GooglePayComponentParamsMapper(CommonComponentParamsMapper())
                .mapToParams(configuration, Locale.US, null, null, paymentMethod),
            analyticsManager = analyticsManager,
            paymentsClient = paymentsClient,
        )
    }

    companion object {
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
        private const val TEST_CHECKOUT_ATTEMPT_ID = "TEST_CHECKOUT_ATTEMPT_ID"
        private const val TEST_PAYMENT_METHOD_TYPE = "TEST_PAYMENT_METHOD_TYPE"
        private val TEST_PAYMENT_DATA: PaymentData =
            PaymentData.fromJson("{\"paymentMethodData\": {\"tokenizationData\": {\"token\": \"test_token\"}}}")

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, expectedComponentStateValue
            arguments(Amount("EUR", 100), Amount("EUR", 100)),
            arguments(Amount("USD", 0), Amount("USD", 0)),
            arguments(null, Amount("USD", 0)),
            arguments(null, Amount("USD", 0)),
        )

        @JvmStatic
        fun paymentResultSource() = listOf(
            arguments(ApiTaskResult(TEST_PAYMENT_DATA, Status.RESULT_SUCCESS), true),
            arguments(ApiTaskResult(null, Status.RESULT_SUCCESS), false),
            arguments(ApiTaskResult(null, Status.RESULT_CANCELED), false),
            arguments(ApiTaskResult(null, Status.RESULT_INTERNAL_ERROR), false),
            arguments(ApiTaskResult(null, Status.RESULT_INTERRUPTED), false),
            arguments(ApiTaskResult(null, Status(AutoResolveHelper.RESULT_ERROR)), false),
        )
    }
}
