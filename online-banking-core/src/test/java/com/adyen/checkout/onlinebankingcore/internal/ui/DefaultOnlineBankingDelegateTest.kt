/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 15/2/2023.
 */

package com.adyen.checkout.onlinebankingcore.internal.ui

import android.content.Context
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
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.onlinebankingcore.internal.ui.model.OnlineBankingModel
import com.adyen.checkout.onlinebankingcore.internal.ui.model.OnlineBankingOutputData
import com.adyen.checkout.onlinebankingcore.utils.TestOnlineBankingComponentState
import com.adyen.checkout.onlinebankingcore.utils.TestOnlineBankingConfiguration
import com.adyen.checkout.onlinebankingcore.utils.TestOnlineBankingPaymentMethod
import com.adyen.checkout.ui.core.old.internal.ui.SubmitHandler
import com.adyen.checkout.ui.core.old.internal.util.PdfOpener
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
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultOnlineBankingDelegateTest(
    @Mock private val context: Context,
    @Mock private val pdfOpener: PdfOpener,
    @Mock private val submitHandler: SubmitHandler<TestOnlineBankingComponentState>,
) {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var delegate: DefaultOnlineBankingDelegate<
        TestOnlineBankingPaymentMethod,
        TestOnlineBankingComponentState,
        >

    @BeforeEach
    fun setup() {
        analyticsManager = TestAnalyticsManager()
        delegate = createOnlineBankingDelegate()
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `selectedIssuer is null, then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData { selectedIssuer = null }

                with(expectMostRecentItem()) {
                    assertNull(selectedIssuer)
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `selectedIssuer is valid, then output should be valid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData { selectedIssuer = OnlineBankingModel(id = "id", name = "test") }

                with(expectMostRecentItem()) {
                    assertEquals("test", selectedIssuer?.name)
                    assertEquals("id", selectedIssuer?.id)
                    assertTrue(isValid)
                }
            }
        }
    }

    @Nested
    @DisplayName("when creating component state and")
    inner class CreateComponentStateTest {
        @Test
        fun `output is invalid, then component state should be invalid`() = runTest {
            delegate.componentStateFlow.test {
                val output = OnlineBankingOutputData(null)

                delegate.updateComponentState(output)

                with(expectMostRecentItem()) {
                    assertFalse(isInputValid)
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `output is valid, then component state should be valid`() = runTest {
            delegate.componentStateFlow.test {
                val model = OnlineBankingModel(id = "issuer-id", name = "issuer-name")
                val output = OnlineBankingOutputData(model)

                delegate.updateComponentState(output)

                with(expectMostRecentItem()) {
                    assertEquals("issuer-id", data.paymentMethod?.issuer)
                    assertEquals(TEST_ORDER, data.order)
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                }
            }
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.onlinebankingcore.internal.ui.DefaultOnlineBankingDelegateTest#amountSource")
        fun `when input data is valid then amount is propagated in component state if set`(
            configurationValue: Amount?,
            expectedComponentStateValue: Amount?,
        ) = runTest {
            if (configurationValue != null) {
                val configuration = createCheckoutConfiguration(configurationValue)
                delegate = createOnlineBankingDelegate(configuration = configuration)
            }
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.componentStateFlow.test {
                delegate.updateInputData {
                    selectedIssuer = OnlineBankingModel(id = "id", name = "test")
                }
                assertEquals(expectedComponentStateValue, expectMostRecentItem().data.amount)
            }
        }
    }

    @Nested
    @DisplayName("when opening terms and conditions and it")
    inner class TermsAndConditionsTest {
        @Test
        fun `successfully opens`() {
            val url = TEST_URL

            delegate.openTermsAndConditions(context)

            verify(pdfOpener).open(context, url)
        }

        @Test
        fun `failed to open pdf and throws an exception`() {
            val url = TEST_URL
            whenever(pdfOpener.open(context, url)) doThrow IllegalStateException("failed")

            delegate.openTermsAndConditions(context)

            assertThrows<IllegalStateException> { pdfOpener.open(context, url) }
        }
    }

    @Nested
    inner class SubmitButtonVisibilityTest {

        @Test
        fun `when submit button is configured to be hidden, then it should not show`() {
            delegate = createOnlineBankingDelegate(
                configuration = createCheckoutConfiguration {
                    setSubmitButtonVisible(false)
                },
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `when submit button is configured to be visible, then it should show`() {
            delegate = createOnlineBankingDelegate(
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
            val submitFlow = flow<TestOnlineBankingComponentState> { emit(mock()) }
            whenever(submitHandler.submitFlow) doReturn submitFlow
            val delegate = createOnlineBankingDelegate()

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
                delegate.updateInputData {
                    selectedIssuer = OnlineBankingModel(id = "id", name = "test")
                }

                assertEquals(TEST_CHECKOUT_ATTEMPT_ID, expectMostRecentItem().data.paymentMethod?.checkoutAttemptId)
            }
        }

        @Test
        fun `when delegate is cleared then analytics manager is cleared`() {
            delegate.onCleared()

            analyticsManager.assertIsCleared()
        }
    }

    private fun createOnlineBankingDelegate(
        configuration: CheckoutConfiguration = createCheckoutConfiguration(),
        order: OrderRequest? = TEST_ORDER,
    ) = DefaultOnlineBankingDelegate(
        observerRepository = PaymentObserverRepository(),
        pdfOpener = pdfOpener,
        paymentMethod = PaymentMethod(type = TEST_PAYMENT_METHOD_TYPE),
        order = order,
        analyticsManager = analyticsManager,
        componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper())
            .mapToParams(
                checkoutConfiguration = configuration,
                deviceLocale = Locale.US,
                dropInOverrideParams = null,
                componentSessionParams = null,
                componentConfiguration = configuration.getConfiguration(TEST_CONFIGURATION_KEY),
            ),
        termsAndConditionsUrl = TEST_URL,
        paymentMethodFactory = { TestOnlineBankingPaymentMethod() },
        submitHandler = submitHandler,
        componentStateFactory = { data, isInputValid, isReady ->
            TestOnlineBankingComponentState(
                data = data,
                isInputValid = isInputValid,
                isReady = isReady,
            )
        },
    )

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: TestOnlineBankingConfiguration.Builder.() -> Unit = {}
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        amount = amount,
    ) {
        val testConfiguration = TestOnlineBankingConfiguration.Builder(shopperLocale, environment, clientKey)
            .apply(configuration)
            .build()
        addConfiguration(TEST_CONFIGURATION_KEY, testConfiguration)
    }

    companion object {
        private const val TEST_CONFIGURATION_KEY = "TEST_CONFIGURATION_KEY"
        private const val TEST_URL = "any-url"
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
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
