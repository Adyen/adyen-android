/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 25/7/2022.
 */

package com.adyen.checkout.blik.internal.ui

import app.cash.turbine.test
import com.adyen.checkout.blik.BlikComponentState
import com.adyen.checkout.blik.BlikConfiguration
import com.adyen.checkout.blik.blikConfiguration
import com.adyen.checkout.blik.getBlikConfiguration
import com.adyen.checkout.blik.internal.ui.model.BlikOutputData
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParamsMapper
import com.adyen.checkout.core.AdyenLogger
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.internal.util.Logger
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultBlikDelegateTest(
    @Mock private val analyticsRepository: AnalyticsRepository,
    @Mock private val submitHandler: SubmitHandler<BlikComponentState>,
) {

    private lateinit var delegate: DefaultBlikDelegate

    @BeforeEach
    fun beforeEach() {
        delegate = createBlikDelegate()
        AdyenLogger.setLogLevel(Logger.NONE)
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `input is invalid, then output data should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                skipItems(1)
                delegate.updateInputData {
                    blikCode = ""
                }

                with(awaitItem()) {
                    assertEquals("", blikCodeField.value)
                    assertFalse(isValid)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `input is invalid, then component state should be invalid`() = runTest {
            delegate.componentStateFlow.test {
                skipItems(1)
                delegate.updateInputData {
                    blikCode = "1234"
                }

                with(awaitItem()) {
                    assertEquals("1234", data.paymentMethod?.blikCode)
                    assertFalse(isInputValid)
                    assertFalse(isValid)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `input is valid, then output data should be propagated`() = runTest {
            delegate.outputDataFlow.test {
                skipItems(1)
                delegate.updateInputData {
                    blikCode = "545897"
                }

                with(awaitItem()) {
                    assertEquals("545897", blikCodeField.value)
                    assertTrue(isValid)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `input is valid, then component state should be propagated`() = runTest {
            delegate.componentStateFlow.test {
                skipItems(1)
                delegate.updateInputData {
                    blikCode = "123243"
                }

                with(awaitItem()) {
                    assertEquals("123243", data.paymentMethod?.blikCode)
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("when creating component state and")
    inner class CreateComponentStateTest {

        @Test
        fun `output data is invalid, then component state should be invalid`() = runTest {
            delegate.componentStateFlow.test {
                skipItems(1)
                delegate.updateComponentState(BlikOutputData("87909090"))

                with(awaitItem()) {
                    assertEquals("87909090", data.paymentMethod?.blikCode)
                    assertFalse(isInputValid)
                    assertFalse(isValid)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `output data is valid, then component state should be propagated`() = runTest {
            delegate.componentStateFlow.test {
                delegate.updateComponentState(BlikOutputData("777134"))

                with(expectMostRecentItem()) {
                    assertEquals("777134", data.paymentMethod?.blikCode)
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                    assertEquals(TEST_ORDER, data.order)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.blik.internal.ui.DefaultBlikDelegateTest#amountSource")
        fun `when input data is valid then amount is propagated in component state if set`(
            configurationValue: Amount?,
            expectedComponentStateValue: Amount?,
        ) = runTest {
            if (configurationValue != null) {
                val configuration = createCheckoutConfiguration(configurationValue)
                delegate = createBlikDelegate(configuration = configuration)
            }
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.componentStateFlow.test {
                delegate.updateInputData {
                    blikCode = "545897"
                }
                assertEquals(expectedComponentStateValue, expectMostRecentItem().data.amount)
            }
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
            delegate = createBlikDelegate(
                configuration = createCheckoutConfiguration {
                    setSubmitButtonVisible(false)
                },
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `when submit button is configured to be visible, then it should show`() {
            delegate = createBlikDelegate(
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
        fun `when component state is valid then PaymentMethodDetails should contain checkoutAttemptId`() = runTest {
            whenever(analyticsRepository.getCheckoutAttemptId()) doReturn TEST_CHECKOUT_ATTEMPT_ID

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateInputData {
                    blikCode = "545897"
                }

                assertEquals(TEST_CHECKOUT_ATTEMPT_ID, expectMostRecentItem().data.paymentMethod?.checkoutAttemptId)
            }
        }
    }

    private fun createBlikDelegate(
        configuration: CheckoutConfiguration = createCheckoutConfiguration()
    ) = DefaultBlikDelegate(
        observerRepository = PaymentObserverRepository(),
        componentParams = ButtonComponentParamsMapper(false, null).mapToParams(
            configuration,
            configuration.getBlikConfiguration(),
            null,
        ),
        paymentMethod = PaymentMethod(),
        order = TEST_ORDER,
        analyticsRepository = analyticsRepository,
        submitHandler = submitHandler,
    )

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: BlikConfiguration.Builder.() -> Unit = {}
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        amount = amount,
    ) {
        blikConfiguration(configuration)
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
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
