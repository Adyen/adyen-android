/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 28/2/2023.
 */

package com.adyen.checkout.upi.internal.ui

import app.cash.turbine.test
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.App
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.Environment
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.extensions.test
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.upi.UPIComponentState
import com.adyen.checkout.upi.UPIConfiguration
import com.adyen.checkout.upi.getUPIConfiguration
import com.adyen.checkout.upi.internal.ui.model.UPIIntentItem
import com.adyen.checkout.upi.internal.ui.model.UPIMode
import com.adyen.checkout.upi.internal.ui.model.UPIOutputData
import com.adyen.checkout.upi.internal.ui.model.UPISelectedMode
import com.adyen.checkout.upi.upi
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, LoggingExtension::class)
internal class DefaultUPIDelegateTest(
    @Mock private val submitHandler: SubmitHandler<UPIComponentState>,
) {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var delegate: DefaultUPIDelegate

    @BeforeEach
    fun beforeEach() {
        analyticsManager = TestAnalyticsManager()
        delegate = createUPIDelegate()
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `intent apps are not present, then availableModes should not contain vpa and qr modes`() = runTest {
            val paymentMethod = PaymentMethod(
                apps = listOf(
                    App("id1", "name1"),
                    App("id2", "name2"),
                ),
            )
            val intentItemList = listOf(
                UPIIntentItem.PaymentApp("id1", "name1", Environment.TEST),
                UPIIntentItem.PaymentApp("id2", "name2", Environment.TEST),
                UPIIntentItem.GenericApp,
                UPIIntentItem.ManualInput(null),
            )
            val expectedAvailableModes = listOf(
                UPIMode.Intent(intentItemList, intentItemList.firstOrNull()),
                UPIMode.Qr,
            )
            val delegate = createUPIDelegate(paymentMethod = paymentMethod)
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                selectedUPIIntentItem = intentItemList.first()
            }

            assertEquals(expectedAvailableModes, outputTestFlow.latestValue.availableModes)

            outputTestFlow.cancel()
        }

        @Test
        fun `intent apps are present, then availableModes should contain intent and qr modes`() = runTest {
            val expectedAvailableModes = listOf(
                UPIMode.Vpa,
                UPIMode.Qr,
            )
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {}

            assertEquals(expectedAvailableModes, outputTestFlow.latestValue.availableModes)

            outputTestFlow.cancel()
        }

        @Test
        fun `mode is INTENT and selected upi intent item is null, then output should be invalid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                selectedMode = UPISelectedMode.INTENT
                selectedUPIIntentItem = null
            }

            assertFalse(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }

        @Test
        fun `mode is INTENT and selected upi intent item is PaymentApp, then output should be valid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                selectedMode = UPISelectedMode.INTENT
                selectedUPIIntentItem = UPIIntentItem.PaymentApp("id", "name", Environment.TEST)
            }

            assertTrue(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }

        @Test
        fun `mode is INTENT and selected upi intent item is GenericApp, then output should be valid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                selectedMode = UPISelectedMode.INTENT
                selectedUPIIntentItem = UPIIntentItem.GenericApp
            }

            assertTrue(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }

        @Test
        fun `mode is INTENT and selected upi intent item is ManualInput and address is empty, then output should be invalid`() =
            runTest {
                val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

                delegate.updateInputData {
                    selectedMode = UPISelectedMode.INTENT
                    selectedUPIIntentItem = UPIIntentItem.ManualInput(null)
                    intentVirtualPaymentAddress = " "
                }

                assertFalse(outputTestFlow.latestValue.isValid)

                outputTestFlow.cancel()
            }

        @Test
        fun `mode is INTENT and selected upi intent item is ManualInput and address is not empty, then output should be valid`() =
            runTest {
                val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

                delegate.updateInputData {
                    selectedMode = UPISelectedMode.INTENT
                    selectedUPIIntentItem = UPIIntentItem.ManualInput(null)
                    intentVirtualPaymentAddress = "address"
                }

                assertTrue(outputTestFlow.latestValue.isValid)

                outputTestFlow.cancel()
            }

        @Test
        fun `mode is VPA and address is empty, then output should be invalid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                selectedMode = UPISelectedMode.VPA
                vpaVirtualPaymentAddress = " "
            }

            assertFalse(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }

        @Test
        fun `mode is VPA and address is some value, then output should be valid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                selectedMode = UPISelectedMode.VPA
                vpaVirtualPaymentAddress = "address"
            }

            assertTrue(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }

        @Test
        fun `mode is QR, then output should be valid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                selectedMode = UPISelectedMode.QR
                vpaVirtualPaymentAddress = ""
            }

            assertTrue(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }
    }

    @Nested
    @DisplayName("when creating component state and")
    inner class CreateComponentStateTest {

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.upi.internal.ui.DefaultUPIDelegateTest#invalidOutputDataSource")
        fun `output is invalid, then component state should be invalid`(
            invalidOutputData: UPIOutputData
        ) = runTest {
            val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)

            delegate.updateComponentState(invalidOutputData)

            with(componentStateTestFlow.latestValue) {
                assertFalse(isInputValid)
                assertFalse(isValid)
            }
        }

        @Test
        fun `mode is INTENT and selected intent item is payment app, then component state should be valid`() = runTest {
            val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)
            val paymentApp = UPIIntentItem.PaymentApp("id", "name", Environment.TEST)
            val outputData = createOutputData(
                selectedMode = UPISelectedMode.INTENT,
                selectedUPIIntentItem = paymentApp,
            )

            delegate.updateComponentState(outputData)

            with(componentStateTestFlow.latestValue) {
                assertEquals(PaymentMethodTypes.UPI_INTENT, data.paymentMethod?.type)
                assertEquals(paymentApp.id, data.paymentMethod?.appId)
                assertNull(data.paymentMethod?.virtualPaymentAddress)
                assertEquals(TEST_ORDER, data.order)
                assertTrue(isInputValid)
                assertTrue(isValid)
            }
        }

        @Test
        fun `mode is INTENT and selected intent item is generic app, then component state should be valid`() = runTest {
            val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)
            val outputData = createOutputData(
                selectedMode = UPISelectedMode.INTENT,
                selectedUPIIntentItem = UPIIntentItem.GenericApp,
            )

            delegate.updateComponentState(outputData)

            with(componentStateTestFlow.latestValue) {
                assertEquals(PaymentMethodTypes.UPI_INTENT, data.paymentMethod?.type)
                assertNull(data.paymentMethod?.appId)
                assertNull(data.paymentMethod?.virtualPaymentAddress)
                assertEquals(TEST_ORDER, data.order)
                assertTrue(isInputValid)
                assertTrue(isValid)
            }
        }

        @Test
        fun `mode is INTENT and selected intent item is manual input, then component state should be valid`() =
            runTest {
                val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)
                val outputData = createOutputData(
                    selectedMode = UPISelectedMode.INTENT,
                    selectedUPIIntentItem = UPIIntentItem.ManualInput(null),
                    intentVirtualPaymentAddressFieldState = FieldState("test", Validation.Valid),
                )

                delegate.updateComponentState(outputData)

                with(componentStateTestFlow.latestValue) {
                    assertEquals(PaymentMethodTypes.UPI_COLLECT, data.paymentMethod?.type)
                    assertNull(data.paymentMethod?.appId)
                    assertEquals("test", data.paymentMethod?.virtualPaymentAddress)
                    assertEquals(TEST_ORDER, data.order)
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                }
            }

        @Test
        fun `mode is VPA and output is valid, then component state should be valid`() = runTest {
            val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)
            val outputData = createOutputData(
                selectedMode = UPISelectedMode.VPA,
                virtualPaymentAddressFieldState = FieldState("test", Validation.Valid),
            )

            delegate.updateComponentState(outputData)

            with(componentStateTestFlow.latestValue) {
                assertEquals("test", data.paymentMethod?.virtualPaymentAddress)
                assertEquals(PaymentMethodTypes.UPI_COLLECT, data.paymentMethod?.type)
                assertNull(data.paymentMethod?.appId)
                assertEquals(TEST_ORDER, data.order)
                assertTrue(isInputValid)
                assertTrue(isValid)
            }
        }

        @Test
        fun `mode is QR and output is valid, then component state should be valid`() = runTest {
            val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)
            val outputData = createOutputData(
                selectedMode = UPISelectedMode.QR,
            )

            delegate.updateComponentState(outputData)

            with(componentStateTestFlow.latestValue) {
                assertNull(data.paymentMethod?.virtualPaymentAddress)
                assertEquals(PaymentMethodTypes.UPI_QR, data.paymentMethod?.type)
                assertNull(data.paymentMethod?.appId)
                assertEquals(TEST_ORDER, data.order)
                assertTrue(isInputValid)
                assertTrue(isValid)
            }
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.upi.internal.ui.DefaultUPIDelegateTest#amountSource")
        fun `when input data is valid then amount is propagated in component state if set`(
            configurationValue: Amount?,
            expectedComponentStateValue: Amount?,
        ) = runTest {
            if (configurationValue != null) {
                val configuration = createCheckoutConfiguration(configurationValue)
                delegate = createUPIDelegate(configuration = configuration)
            }
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.componentStateFlow.test {
                delegate.updateInputData {
                    selectedMode = UPISelectedMode.VPA
                    vpaVirtualPaymentAddress = "somevpa"
                }
                assertEquals(expectedComponentStateValue, expectMostRecentItem().data.amount)
            }
        }
    }

    @Nested
    inner class VirtualPaymentAddressCacheTest {

        @Test
        fun `when updateIntentVirtualPaymentAddress is called, then payment address is cleared`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.updateIntentVirtualPaymentAddress("test_address")

            delegate.outputDataFlow.test {
                assertEquals("", expectMostRecentItem().intentVirtualPaymentAddressFieldState.value)
            }
        }

        @Test
        fun `when onSubmit is called, then payment address is being updated with the cache`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.updateIntentVirtualPaymentAddress("test_address")

            delegate.onSubmit()

            delegate.outputDataFlow.test {
                assertEquals("test_address", expectMostRecentItem().intentVirtualPaymentAddressFieldState.value)
            }
        }
    }

    @Nested
    inner class SubmitButtonVisibilityTest {

        @Test
        fun `when submit button is configured to be hidden, then it should not show`() {
            delegate = createUPIDelegate(
                configuration = createCheckoutConfiguration {
                    setSubmitButtonVisible(false)
                },
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `when submit button is configured to be visible, then it should show`() {
            delegate = createUPIDelegate(
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
        fun `when selected mode is INTENT and there is no selected upi intent item, then submit button should not be enabled`() {
            delegate.updateInputData {
                selectedMode = UPISelectedMode.INTENT
                selectedUPIIntentItem = null
            }

            assertFalse(delegate.shouldEnableSubmitButton())
        }

        @Test
        fun `when selected mode is INTENT and there is selected upi intent item, then submit button should be enabled`() {
            delegate.updateInputData {
                selectedMode = UPISelectedMode.INTENT
                selectedUPIIntentItem = UPIIntentItem.GenericApp
            }

            assertTrue(delegate.shouldEnableSubmitButton())
        }

        @Test
        fun `when selected mode is VPA, then submit button should be enabled`() {
            delegate.updateInputData {
                selectedMode = UPISelectedMode.VPA
            }

            assertTrue(delegate.shouldEnableSubmitButton())
        }

        @Test
        fun `when selected mode is QR, then submit button should be enabled`() {
            delegate.updateInputData {
                selectedMode = UPISelectedMode.QR
            }

            assertTrue(delegate.shouldEnableSubmitButton())
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
        fun `when onSubmit is called, then submit event is tracked`() {
            delegate.onSubmit()

            val expectedEvent = GenericEvents.submit(TEST_PAYMENT_METHOD_TYPE)
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when component state is valid then PaymentMethodDetails should contain checkoutAttemptId`() = runTest {
            analyticsManager.setCheckoutAttemptId(TEST_CHECKOUT_ATTEMPT_ID)

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateInputData {
                    selectedMode = UPISelectedMode.QR
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

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: UPIConfiguration.Builder.() -> Unit = {}
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        amount = amount,
    ) {
        upi(configuration)
    }

    private fun createUPIDelegate(
        order: Order? = TEST_ORDER,
        paymentMethod: PaymentMethod = PaymentMethod(TEST_PAYMENT_METHOD_TYPE),
        configuration: CheckoutConfiguration = createCheckoutConfiguration(),
    ) = DefaultUPIDelegate(
        submitHandler = submitHandler,
        analyticsManager = analyticsManager,
        observerRepository = PaymentObserverRepository(),
        paymentMethod = paymentMethod,
        order = order,
        componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = Locale.US,
            dropInOverrideParams = null,
            componentSessionParams = null,
            componentConfiguration = configuration.getUPIConfiguration(),
        ),
    )

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
        private const val TEST_CHECKOUT_ATTEMPT_ID = "TEST_CHECKOUT_ATTEMPT_ID"
        private const val TEST_PAYMENT_METHOD_TYPE = "TEST_PAYMENT_METHOD_TYPE"

        @JvmStatic
        fun invalidOutputDataSource() = listOf(
            // invalidOutputData
            arguments(
                createOutputData(
                    selectedMode = UPISelectedMode.INTENT,
                    selectedUPIIntentItem = null,
                ),
            ),
            arguments(
                createOutputData(
                    selectedMode = UPISelectedMode.INTENT,
                    selectedUPIIntentItem = UPIIntentItem.ManualInput(null),
                    intentVirtualPaymentAddressFieldState = FieldState("", Validation.Invalid(0)),
                ),
            ),
            arguments(
                createOutputData(
                    selectedMode = UPISelectedMode.VPA,
                    virtualPaymentAddressFieldState = FieldState("", Validation.Invalid(0)),
                ),
            ),
        )

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, expectedComponentStateValue
            arguments(Amount("EUR", 100), Amount("EUR", 100)),
            arguments(Amount("USD", 0), Amount("USD", 0)),
            arguments(null, null),
            arguments(null, null),
        )

        private fun createOutputData(
            availableModes: List<UPIMode> = listOf(),
            selectedMode: UPISelectedMode = UPISelectedMode.VPA,
            selectedUPIIntentItem: UPIIntentItem? = null,
            virtualPaymentAddressFieldState: FieldState<String> = FieldState("test", Validation.Invalid(0)),
            intentVirtualPaymentAddressFieldState: FieldState<String> = FieldState("test", Validation.Invalid(0)),
        ) = UPIOutputData(
            availableModes = availableModes,
            selectedMode = selectedMode,
            selectedUPIIntentItem = selectedUPIIntentItem,
            virtualPaymentAddressFieldState = virtualPaymentAddressFieldState,
            intentVirtualPaymentAddressFieldState = intentVirtualPaymentAddressFieldState,
        )
    }
}
