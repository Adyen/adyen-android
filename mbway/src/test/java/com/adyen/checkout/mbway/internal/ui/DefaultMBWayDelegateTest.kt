/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 25/7/2022.
 */

package com.adyen.checkout.mbway.internal.ui

import app.cash.turbine.test
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.analytics.AnalyticsRepository
import com.adyen.checkout.components.base.ButtonComponentParamsMapper
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.MBWayPaymentMethod
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.ui.SubmitHandler
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.mbway.MBWayConfiguration
import com.adyen.checkout.mbway.internal.ui.model.MBWayOutputData
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
import org.mockito.kotlin.verify
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultMBWayDelegateTest(
    @Mock private val analyticsRepository: AnalyticsRepository,
    @Mock private val submitHandler: SubmitHandler<PaymentComponentState<MBWayPaymentMethod>>,
) {

    private lateinit var delegate: DefaultMBWayDelegate

    @BeforeEach
    fun beforeEach() {
        delegate = createMBWayDelegate()
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `input is invalid, then output data should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                skipItems(1)
                delegate.updateInputData {
                    countryCode = "+1"
                    localPhoneNumber = "04023456"
                }

                with(awaitItem()) {
                    assertEquals("+14023456", mobilePhoneNumberFieldState.value)
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
                    countryCode = "+23"
                    localPhoneNumber = "0056778"
                }

                with(awaitItem()) {
                    assertEquals("+2356778", data.paymentMethod?.telephoneNumber)
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
                    countryCode = "+351"
                    localPhoneNumber = "234567890"
                }

                with(awaitItem()) {
                    assertEquals("+351234567890", mobilePhoneNumberFieldState.value)
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
                    countryCode = "+1"
                    localPhoneNumber = "9257348920"
                }

                with(awaitItem()) {
                    assertEquals("+19257348920", data.paymentMethod?.telephoneNumber)
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
                delegate.updateComponentState(MBWayOutputData("+7867676"))

                with(awaitItem()) {
                    assertEquals("+7867676", data.paymentMethod?.telephoneNumber)
                    assertFalse(isInputValid)
                    assertFalse(isValid)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `output data is valid, then component state should be propagated`() = runTest {
            delegate.componentStateFlow.test {
                skipItems(1)
                delegate.updateComponentState(MBWayOutputData("+31666666666"))

                with(awaitItem()) {
                    assertEquals("+31666666666", data.paymentMethod?.telephoneNumber)
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                    assertEquals(TEST_ORDER, data.order)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `when delegate is initialized then analytics event is sent`() = runTest {
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        verify(analyticsRepository).sendAnalyticsEvent()
    }

    @Nested
    inner class SubmitButtonVisibilityTest {

        @Test
        fun `when submit button is configured to be hidden, then it should not show`() {
            delegate = createMBWayDelegate(
                configuration = getDefaultMBWayConfigurationBuilder()
                    .setSubmitButtonVisible(false)
                    .build()
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `when submit button is configured to be visible, then it should show`() {
            delegate = createMBWayDelegate(
                configuration = getDefaultMBWayConfigurationBuilder()
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

    private fun createMBWayDelegate(
        configuration: MBWayConfiguration = getDefaultMBWayConfigurationBuilder().build()
    ) = DefaultMBWayDelegate(
        observerRepository = PaymentObserverRepository(),
        paymentMethod = PaymentMethod(),
        order = TEST_ORDER,
        componentParams = ButtonComponentParamsMapper().mapToParams(configuration),
        analyticsRepository = analyticsRepository,
        submitHandler = submitHandler,
    )

    private fun getDefaultMBWayConfigurationBuilder() = MBWayConfiguration.Builder(
        Locale.US,
        Environment.TEST,
        TEST_CLIENT_KEY
    )

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
    }
}
