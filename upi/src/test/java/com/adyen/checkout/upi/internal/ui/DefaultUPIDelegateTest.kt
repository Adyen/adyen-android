/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 28/2/2023.
 */

package com.adyen.checkout.upi.internal.ui

import app.cash.turbine.test
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParamsMapper
import com.adyen.checkout.components.core.internal.util.PaymentMethodTypes
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.test.extensions.test
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.upi.UPIComponentState
import com.adyen.checkout.upi.UPIConfiguration
import com.adyen.checkout.upi.internal.ui.model.UPIMode
import com.adyen.checkout.upi.internal.ui.model.UPIOutputData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
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
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultUPIDelegateTest(
    @Mock private val submitHandler: SubmitHandler<UPIComponentState>,
    @Mock private val analyticsRepository: AnalyticsRepository,
) {

    private lateinit var delegate: DefaultUPIDelegate

    private val configuration = UPIConfiguration.Builder(
        Locale.US,
        Environment.TEST,
        TEST_CLIENT_KEY
    ).build()

    @BeforeEach
    fun beforeEach() {
        delegate = createUPIDelegate()
        Logger.setLogcatLevel(Logger.NONE)
    }

    @Test
    fun `when delegate is initialized then analytics event is sent`() = runTest {
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        verify(analyticsRepository).sendAnalyticsEvent()
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `mode is VPA and VPA is empty, then output should be invalid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                mode = UPIMode.VPA
                virtualPaymentAddress = " "
            }

            assertFalse(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }

        @Test
        fun `mode is VPA and VPA is some value, then output should be valid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                mode = UPIMode.VPA
                virtualPaymentAddress = "somevpa"
            }

            assertTrue(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }

        @Test
        fun `mode is QR, then output should be valid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                mode = UPIMode.QR
                virtualPaymentAddress = ""
            }

            assertTrue(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }
    }

    @Nested
    @DisplayName("when creating component state and")
    inner class CreateComponentStateTest {

        @Test
        fun `output is invalid, then component state should be invalid`() = runTest {
            val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)

            delegate.updateComponentState(UPIOutputData(UPIMode.VPA, ""))

            with(componentStateTestFlow.latestValue) {
                assertFalse(isInputValid)
                assertFalse(isValid)
            }
        }

        @Test
        fun `mode is VPA and output is valid, then component state should be valid`() = runTest {
            val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)

            delegate.updateComponentState(UPIOutputData(UPIMode.VPA, "test"))

            with(componentStateTestFlow.latestValue) {
                assertEquals("test", data.paymentMethod?.virtualPaymentAddress)
                assertEquals(PaymentMethodTypes.UPI_COLLECT, data.paymentMethod?.type)
                assertEquals(TEST_ORDER, data.order)
                assertTrue(isInputValid)
                assertTrue(isValid)
            }
        }

        @Test
        fun `mode is QR and output is valid, then component state should be valid`() = runTest {
            val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)

            delegate.updateComponentState(UPIOutputData(UPIMode.QR, ""))

            with(componentStateTestFlow.latestValue) {
                assertNull(data.paymentMethod?.virtualPaymentAddress)
                assertEquals(PaymentMethodTypes.UPI_QR, data.paymentMethod?.type)
                assertEquals(TEST_ORDER, data.order)
                assertTrue(isInputValid)
                assertTrue(isValid)
            }
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

    private fun createUPIDelegate(
        order: Order? = TEST_ORDER
    ) = DefaultUPIDelegate(
        submitHandler = submitHandler,
        analyticsRepository = analyticsRepository,
        observerRepository = PaymentObserverRepository(),
        paymentMethod = PaymentMethod(),
        order = order,
        componentParams = ButtonComponentParamsMapper(null, null).mapToParams(configuration, null),
    )

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
    }
}
