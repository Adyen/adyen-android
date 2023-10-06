/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 17/8/2022.
 */

package com.adyen.checkout.issuerlist.internal.ui

import app.cash.turbine.test
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.core.AdyenLogger
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.issuerlist.IssuerListViewType
import com.adyen.checkout.issuerlist.TestIssuerComponentState
import com.adyen.checkout.issuerlist.internal.IssuerListConfiguration
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerListComponentParamsMapper
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerListOutputData
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerModel
import com.adyen.checkout.issuerlist.utils.TestIssuerListConfiguration
import com.adyen.checkout.issuerlist.utils.TestIssuerPaymentMethod
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultIssuerListDelegateTest(
    @Mock private val analyticsRepository: AnalyticsRepository,
    @Mock private val submitHandler: SubmitHandler<TestIssuerComponentState>,
) {

    private lateinit var delegate: DefaultIssuerListDelegate<TestIssuerPaymentMethod, TestIssuerComponentState>

    @BeforeEach
    fun beforeEach() {
        delegate = createIssuerListDelegate()
        AdyenLogger.setLogLevel(Logger.NONE)
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `selectedIssuer is null, then output should be null`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData { selectedIssuer = null }

                with(expectMostRecentItem()) {
                    assertNull(selectedIssuer)
                }
            }
        }

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
                delegate.updateInputData {
                    selectedIssuer = IssuerModel(id = "id", name = "test", environment = Environment.TEST)
                }

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
                delegate.updateComponentState(IssuerListOutputData(null))
                with(expectMostRecentItem()) {
                    assertFalse(isInputValid)
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `output is valid, then component state should be valid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.updateComponentState(
                    IssuerListOutputData(
                        IssuerModel(
                            id = "issuer-id",
                            name = "issuer-name",
                            environment = Environment.TEST
                        )
                    )
                )
                with(expectMostRecentItem()) {
                    assertEquals("issuer-id", data.paymentMethod?.issuer)
                    assertEquals(TEST_ORDER, data.order)
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                }
            }
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.issuerlist.internal.ui.DefaultIssuerListDelegateTest#amountSource")
        fun `when input data is valid then amount is propagated in component state if set`(
            configurationValue: Amount?,
            expectedComponentStateValue: Amount?,
        ) = runTest {
            if (configurationValue != null) {
                val configuration = getDefaultTestIssuerListConfigurationBuilder()
                    .setAmount(configurationValue)
                    .build()
                delegate = createIssuerListDelegate(configuration = configuration)
            }
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.componentStateFlow.test {
                delegate.updateInputData {
                    selectedIssuer = IssuerModel(id = "id", name = "test", environment = Environment.TEST)
                }
                assertEquals(expectedComponentStateValue, expectMostRecentItem().data.amount)
            }
        }
    }

    @Test
    fun `when configuration viewType is RECYCLER_VIEW then viewFlow should emit RECYCLER_VIEW`() = runTest {
        val configuration: IssuerListConfiguration = TestIssuerListConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        )
            .setViewType(IssuerListViewType.RECYCLER_VIEW)
            .build()

        delegate = DefaultIssuerListDelegate(
            observerRepository = PaymentObserverRepository(),
            componentParams = IssuerListComponentParamsMapper(null, null).mapToParams(configuration, null),
            paymentMethod = PaymentMethod(),
            order = TEST_ORDER,
            analyticsRepository = analyticsRepository,
            submitHandler = submitHandler,
            typedPaymentMethodFactory = { TestIssuerPaymentMethod() },
            componentStateFactory = { data, isInputValid, isReady ->
                TestIssuerComponentState(
                    data = data,
                    isInputValid = isInputValid,
                    isReady = isReady
                )
            }
        )

        delegate.viewFlow.test {
            assertEquals(IssuerListComponentViewType.RecyclerView, expectMostRecentItem())
        }
    }

    @Test
    fun `when configuration viewType is SPINNER_VIEW then viewFlow should emit SPINNER_VIEW`() = runTest {
        val configuration: IssuerListConfiguration = TestIssuerListConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        )
            .setViewType(IssuerListViewType.SPINNER_VIEW)
            .build()

        delegate = DefaultIssuerListDelegate(
            observerRepository = PaymentObserverRepository(),
            componentParams = IssuerListComponentParamsMapper(null, null).mapToParams(configuration, null),
            paymentMethod = PaymentMethod(),
            order = TEST_ORDER,
            analyticsRepository = analyticsRepository,
            submitHandler = submitHandler,
            typedPaymentMethodFactory = { TestIssuerPaymentMethod() },
            componentStateFactory = { data, isInputValid, isReady ->
                TestIssuerComponentState(
                    data = data,
                    isInputValid = isInputValid,
                    isReady = isReady
                )
            }
        )
        delegate.viewFlow.test {
            assertEquals(IssuerListComponentViewType.SpinnerView, expectMostRecentItem())
        }
    }

    @Test
    fun `when delegate is initialized then analytics event is sent`() = runTest {
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        verify(analyticsRepository).setupAnalytics()
    }

    @Nested
    inner class SubmitButtonVisibilityTest {

        @Test
        fun `when submit button is configured to be hidden, then it should not show`() {
            delegate = createIssuerListDelegate(
                configuration = getDefaultTestIssuerListConfigurationBuilder()
                    .setViewType(IssuerListViewType.SPINNER_VIEW)
                    .setSubmitButtonVisible(false)
                    .build()
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `when submit button is configured to be visible, then it should show`() {
            delegate = createIssuerListDelegate(
                configuration = getDefaultTestIssuerListConfigurationBuilder()
                    .setViewType(IssuerListViewType.SPINNER_VIEW)
                    .setSubmitButtonVisible(true)
                    .build()
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
        fun `when component state is valid then PaymentMethodDetails should contain checkoutAttemptId`() = runTest {
            whenever(analyticsRepository.getCheckoutAttemptId()) doReturn TEST_CHECKOUT_ATTEMPT_ID

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateInputData {
                    selectedIssuer = IssuerModel(id = "id", name = "test", environment = Environment.TEST)
                }

                assertEquals(TEST_CHECKOUT_ATTEMPT_ID, expectMostRecentItem().data.paymentMethod?.checkoutAttemptId)
            }
        }
    }

    private fun createIssuerListDelegate(
        configuration: TestIssuerListConfiguration = getDefaultTestIssuerListConfigurationBuilder().build()
    ) = DefaultIssuerListDelegate(
        observerRepository = PaymentObserverRepository(),
        componentParams = IssuerListComponentParamsMapper(null, null).mapToParams(configuration, null),
        paymentMethod = PaymentMethod(),
        order = TEST_ORDER,
        analyticsRepository = analyticsRepository,
        submitHandler = submitHandler,
        typedPaymentMethodFactory = { TestIssuerPaymentMethod() },
        componentStateFactory = { data, isInputValid, isReady -> TestIssuerComponentState(data, isInputValid, isReady) }
    )

    private fun getDefaultTestIssuerListConfigurationBuilder() = TestIssuerListConfiguration.Builder(
        Locale.US,
        Environment.TEST,
        TEST_CLIENT_KEY_1
    )

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
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
