/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 18/2/2025.
 */

package com.adyen.checkout.payto.internal.ui

import app.cash.turbine.test
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.provider.SdkDataProvider
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.Environment
import com.adyen.checkout.payto.PayToComponentState
import com.adyen.checkout.payto.PayToConfiguration
import com.adyen.checkout.payto.getPayToConfiguration
import com.adyen.checkout.payto.internal.ui.model.PayIdType
import com.adyen.checkout.payto.internal.ui.model.PayIdTypeModel
import com.adyen.checkout.payto.internal.ui.model.PayToMode
import com.adyen.checkout.payto.internal.ui.model.PayToOutputData
import com.adyen.checkout.payto.payTo
import com.adyen.checkout.test.extensions.test
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultPayToDelegateTest(
    @Mock private val submitHandler: SubmitHandler<PayToComponentState>,
    @Mock private val sdkDataProvider: SdkDataProvider,
) {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var delegate: DefaultPayToDelegate

    @BeforeEach
    fun beforeEach() {
        analyticsManager = TestAnalyticsManager()
        delegate = createPayToDelegate()
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `mode is PAY_ID and PayIdType is null, then output should be invalid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                mode = PayToMode.PAY_ID
                payIdTypeModel = null
                firstName = "First name"
                lastName = "Last name"
            }

            assertFalse(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }

        @Test
        fun `mode is PAY_ID and PayIdType is PHONE and value is valid, then output should be valid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                mode = PayToMode.PAY_ID
                payIdTypeModel = PayIdTypeModel(PayIdType.PHONE, 0)
                phoneNumber = "412345678"
                firstName = "First name"
                lastName = "Last name"
            }

            assertTrue(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }

        @Test
        fun `mode is PAY_ID and PayIdType is PHONE and value is invalid, then output should be invalid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                mode = PayToMode.PAY_ID
                payIdTypeModel = PayIdTypeModel(PayIdType.PHONE, 0)
                phoneNumber = "abc"
                firstName = "First name"
                lastName = "Last name"
            }

            assertFalse(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }

        @Test
        fun `mode is PAY_ID and PayIdType is EMAIL and value is valid, then output should be valid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                mode = PayToMode.PAY_ID
                payIdTypeModel = PayIdTypeModel(PayIdType.EMAIL, 0)
                emailAddress = "test@example.com"
                firstName = "First name"
                lastName = "Last name"
            }

            assertTrue(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }

        @Test
        fun `mode is PAY_ID and PayIdType is EMAIL and value is invalid, then output should be invalid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                mode = PayToMode.PAY_ID
                payIdTypeModel = PayIdTypeModel(PayIdType.EMAIL, 0)
                emailAddress = "plainaddress"
                firstName = "First name"
                lastName = "Last name"
            }

            assertFalse(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }

        @Test
        fun `mode is PAY_ID and PayIdType is ABN and value is valid, then output should be valid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                mode = PayToMode.PAY_ID
                payIdTypeModel = PayIdTypeModel(PayIdType.ABN, 0)
                abnNumber = "123456789"
                firstName = "First name"
                lastName = "Last name"
            }

            assertTrue(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }

        @Test
        fun `mode is PAY_ID and PayIdType is ABN and value is invalid, then output should be invalid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                mode = PayToMode.PAY_ID
                payIdTypeModel = PayIdTypeModel(PayIdType.ABN, 0)
                abnNumber = "9876543210"
                firstName = "First name"
                lastName = "Last name"
            }

            assertFalse(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }

        @Test
        fun `mode is PAY_ID and PayIdType is ORGANIZATION_ID and value is valid, then output should be valid`() =
            runTest {
                val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

                delegate.updateInputData {
                    mode = PayToMode.PAY_ID
                    payIdTypeModel = PayIdTypeModel(PayIdType.ORGANIZATION_ID, 0)
                    organizationId = "12345678901"
                    firstName = "First name"
                    lastName = "Last name"
                }

                assertTrue(outputTestFlow.latestValue.isValid)

                outputTestFlow.cancel()
            }

        @Test
        fun `mode is PAY_ID and PayIdType is ORGANIZATION_ID and value is invalid, then output should be invalid`() =
            runTest {
                val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

                delegate.updateInputData {
                    mode = PayToMode.PAY_ID
                    payIdTypeModel = PayIdTypeModel(PayIdType.ORGANIZATION_ID, 0)
                    organizationId = "Invalid Org"
                    firstName = "First name"
                    lastName = "Last name"
                }

                assertFalse(outputTestFlow.latestValue.isValid)

                outputTestFlow.cancel()
            }

        @Test
        fun `mode is BSB and account number is invalid, then output should be invalid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                mode = PayToMode.BSB
                bsbAccountNumber = ""
                bsbStateBranch = "Main Branch"
                firstName = "First name"
                lastName = "Last name"
            }

            assertFalse(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }

        @Test
        fun `mode is BSB and state branch is invalid, then output should be invalid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                mode = PayToMode.BSB
                bsbAccountNumber = "123456"
                bsbStateBranch = ""
                firstName = "First name"
                lastName = "Last name"
            }

            assertFalse(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }

        @Test
        fun `mode is BSB and account number and state branch are valid, then output should be valid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                mode = PayToMode.BSB
                bsbStateBranch = "123456"
                bsbAccountNumber = "Main Branch"
                firstName = "First name"
                lastName = "Last name"
            }

            assertTrue(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }

        @Test
        fun `mode is valid and first and last name are invalid, then output should be invalid`() = runTest {
            val outputTestFlow = delegate.outputDataFlow.test(testScheduler)

            delegate.updateInputData {
                mode = PayToMode.PAY_ID
                payIdTypeModel = PayIdTypeModel(PayIdType.PHONE, 0)
                phoneNumber = "412345678"
                firstName = ""
                lastName = ""
            }

            assertFalse(outputTestFlow.latestValue.isValid)

            outputTestFlow.cancel()
        }
    }

    @Nested
    @DisplayName("when creating component state and")
    inner class CreateComponentStateTest {

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.payto.internal.ui.DefaultPayToDelegateTest#invalidOutputDataSource")
        fun `output is invalid, then component state should be invalid`(
            invalidOutputData: PayToOutputData
        ) = runTest {
            val componentStateTestFlow = delegate.componentStateFlow.test(testScheduler)

            delegate.updateComponentState(invalidOutputData)

            with(componentStateTestFlow.latestValue) {
                assertFalse(isInputValid)
                assertFalse(isValid)
            }
        }

        @Test
        fun `mode is PAY_ID and payIdType is PHONE, then component state should be valid`() = runTest {
            delegate.componentStateFlow.test {
                skipItems(1)
                delegate.updateComponentState(
                    createOutputData(
                        mode = PayToMode.PAY_ID,
                        payIdTypeModel = PayIdTypeModel(PayIdType.PHONE, 0),
                        mobilePhoneNumber = "9876543210",
                        firstName = "First name",
                        lastName = "Last name",
                    ),
                )

                with(awaitItem()) {
                    assertEquals("+61-9876543210", data.paymentMethod?.shopperAccountIdentifier)
                    assertEquals("First name", data.shopperName?.firstName)
                    assertEquals("Last name", data.shopperName?.lastName)
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                    assertEquals(TEST_ORDER, data.order)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Suppress("NoEmptyFirstLineInMethodBlock")
        @Test
        fun `mode is PAY_ID and payIdType is EMAIL, then component state should be valid`() = runTest {
            delegate.componentStateFlow.test {
                skipItems(1)
                delegate.updateComponentState(
                    createOutputData(
                        mode = PayToMode.PAY_ID,
                        payIdTypeModel = PayIdTypeModel(PayIdType.EMAIL, 0),
                        emailAddress = "test@adyen.com",
                        firstName = "First name",
                        lastName = "Last name",
                    ),
                )

                with(awaitItem()) {
                    assertEquals("test@adyen.com", data.paymentMethod?.shopperAccountIdentifier)
                    assertEquals("First name", data.shopperName?.firstName)
                    assertEquals("Last name", data.shopperName?.lastName)
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                    assertEquals(TEST_ORDER, data.order)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Suppress("NoEmptyFirstLineInMethodBlock")
        @Test
        fun `mode is PAY_ID and payIdType is ABN, then component state should be valid`() = runTest {
            delegate.componentStateFlow.test {
                skipItems(1)
                delegate.updateComponentState(
                    createOutputData(
                        mode = PayToMode.PAY_ID,
                        payIdTypeModel = PayIdTypeModel(PayIdType.ABN, 0),
                        abnNumber = "000000000",
                        firstName = "First name",
                        lastName = "Last name",
                    ),
                )

                with(awaitItem()) {
                    assertEquals("000000000", data.paymentMethod?.shopperAccountIdentifier)
                    assertEquals("First name", data.shopperName?.firstName)
                    assertEquals("Last name", data.shopperName?.lastName)
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                    assertEquals(TEST_ORDER, data.order)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Suppress("NoEmptyFirstLineInMethodBlock")
        @Test
        fun `mode is PAY_ID and payIdType is ORGANIZATION_ID, then component state should be valid`() = runTest {
            delegate.componentStateFlow.test {
                skipItems(1)
                delegate.updateComponentState(
                    createOutputData(
                        mode = PayToMode.PAY_ID,
                        payIdTypeModel = PayIdTypeModel(PayIdType.ORGANIZATION_ID, 0),
                        organizationId = "12345678901",
                        firstName = "First name",
                        lastName = "Last name",
                    ),
                )

                with(awaitItem()) {
                    assertEquals("12345678901", data.paymentMethod?.shopperAccountIdentifier)
                    assertEquals("First name", data.shopperName?.firstName)
                    assertEquals("Last name", data.shopperName?.lastName)
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                    assertEquals(TEST_ORDER, data.order)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Suppress("NoEmptyFirstLineInMethodBlock")
        @Test
        fun `mode is BSB and output is valid, then component state should be valid`() = runTest {
            delegate.componentStateFlow.test {
                skipItems(1)
                delegate.updateComponentState(
                    createOutputData(
                        mode = PayToMode.BSB,
                        bsbStateBranch = "123456",
                        bsbAccountNumber = "Branch-123",
                        firstName = "First name",
                        lastName = "Last name",
                    ),
                )

                with(awaitItem()) {
                    assertEquals("123456-Branch-123", data.paymentMethod?.shopperAccountIdentifier)
                    assertEquals("First name", data.shopperName?.firstName)
                    assertEquals("Last name", data.shopperName?.lastName)
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                    assertEquals(TEST_ORDER, data.order)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.payto.internal.ui.DefaultPayToDelegateTest#amountSource")
        fun `when input data is valid then amount is propagated in component state if set`(
            configurationValue: Amount?,
            expectedComponentStateValue: Amount?,
        ) = runTest {
            if (configurationValue != null) {
                val configuration = createCheckoutConfiguration(configurationValue)
                delegate = createPayToDelegate(configuration = configuration)
            }
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.componentStateFlow.test {
                delegate.updateInputData {
                    mode = PayToMode.PAY_ID
                    payIdTypeModel = PayIdTypeModel(PayIdType.EMAIL, 0)
                    firstName = "First name"
                    lastName = "Last name"
                }
                assertEquals(expectedComponentStateValue, expectMostRecentItem().data.amount)
            }
        }
    }

    @Nested
    inner class SubmitButtonVisibilityTest {

        @Test
        fun `when submit button is configured to be hidden, then it should not show`() {
            delegate = createPayToDelegate(
                configuration = createCheckoutConfiguration {
                    setSubmitButtonVisible(false)
                },
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `when submit button is configured to be visible, then it should show`() {
            delegate = createPayToDelegate(
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
            val submitFlow = flow<PayToComponentState> { emit(mock()) }
            whenever(submitHandler.submitFlow) doReturn submitFlow
            val delegate = createPayToDelegate()

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
                delegate.updateInputData { }

                assertEquals(TEST_CHECKOUT_ATTEMPT_ID, expectMostRecentItem().data.paymentMethod?.checkoutAttemptId)
            }
        }

        @Test
        fun `when delegate is cleared then analytics manager is cleared`() {
            delegate.onCleared()

            analyticsManager.assertIsCleared()
        }
    }

    private fun createPayToDelegate(
        configuration: CheckoutConfiguration = createCheckoutConfiguration(),
    ) = DefaultPayToDelegate(
        observerRepository = PaymentObserverRepository(),
        paymentMethod = PaymentMethod(type = TEST_PAYMENT_METHOD_TYPE),
        order = TEST_ORDER,
        componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = Locale.US,
            dropInOverrideParams = null,
            componentSessionParams = null,
            componentConfiguration = configuration.getPayToConfiguration(),
        ),
        analyticsManager = analyticsManager,
        submitHandler = submitHandler,
        sdkDataProvider = sdkDataProvider,
    )

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: PayToConfiguration.Builder.() -> Unit = {},
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        amount = amount,
    ) {
        payTo(configuration)
    }

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
                    mode = PayToMode.PAY_ID,
                    payIdTypeModel = null,
                ),
            ),
            arguments(
                createOutputData(
                    mode = PayToMode.PAY_ID,
                    payIdTypeModel = PayIdTypeModel(PayIdType.EMAIL, 0),
                    emailAddress = "",
                ),
            ),
            arguments(
                createOutputData(
                    mode = PayToMode.BSB,
                    bsbAccountNumber = "",
                    bsbStateBranch = "",
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

        @Suppress("LongParameterList")
        private fun createOutputData(
            mode: PayToMode,
            payIdTypeModel: PayIdTypeModel? = null,
            mobilePhoneNumber: String = "",
            emailAddress: String = "",
            abnNumber: String = "",
            organizationId: String = "",
            bsbAccountNumber: String = "",
            bsbStateBranch: String = "",
            firstName: String = "",
            lastName: String = "",
        ) = PayToOutputData(
            mode = mode,
            payIdTypeModel = payIdTypeModel,
            mobilePhoneNumber = mobilePhoneNumber,
            emailAddress = emailAddress,
            abnNumber = abnNumber,
            organizationId = organizationId,
            bsbAccountNumber = bsbAccountNumber,
            bsbStateBranch = bsbStateBranch,
            firstName = firstName,
            lastName = lastName,
        )
    }
}
