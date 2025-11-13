/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 18/8/2022.
 */

package com.adyen.checkout.bacs.internal

import app.cash.turbine.test
import com.adyen.checkout.bacs.BacsDirectDebitComponentState
import com.adyen.checkout.bacs.BacsDirectDebitConfiguration
import com.adyen.checkout.bacs.BacsDirectDebitMode
import com.adyen.checkout.bacs.R
import com.adyen.checkout.bacs.bacsDirectDebit
import com.adyen.checkout.bacs.getBacsDirectDebitConfiguration
import com.adyen.checkout.bacs.internal.ui.BacsComponentViewType
import com.adyen.checkout.bacs.internal.ui.DefaultBacsDirectDebitDelegate
import com.adyen.checkout.bacs.internal.ui.model.BacsDirectDebitOutputData
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
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.Environment
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
@ExtendWith(MockitoExtension::class, LoggingExtension::class)
internal class DefaultBacsDirectDebitDelegateTest(
    @Mock private val submitHandler: SubmitHandler<BacsDirectDebitComponentState>,
    @Mock private val sdkDataProvider: SdkDataProvider,
) {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var delegate: DefaultBacsDirectDebitDelegate

    @BeforeEach
    fun beforeEach() {
        analyticsManager = TestAnalyticsManager()
        delegate = createBacsDelegate()
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `account holder is empty, then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData {
                    holderName = ""
                    bankAccountNumber = "12345678"
                    sortCode = "123456"
                    shopperEmail = "test@adyen.com"
                    isAmountConsentChecked = true
                    isAccountConsentChecked = true
                }

                with(expectMostRecentItem()) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `bank account number length more than 8, then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData {
                    holderName = "test"
                    bankAccountNumber = "1234567890"
                    sortCode = "123456"
                    shopperEmail = "test@adyen.com"
                    isAmountConsentChecked = true
                    isAccountConsentChecked = true
                }

                with(expectMostRecentItem()) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `bank account number length less than 8, then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData {
                    holderName = "test"
                    bankAccountNumber = "1234"
                    sortCode = "123456"
                    shopperEmail = "test@adyen.com"
                    isAmountConsentChecked = true
                    isAccountConsentChecked = true
                }

                with(expectMostRecentItem()) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `bank account number is empty, then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData {
                    holderName = "test"
                    bankAccountNumber = ""
                    sortCode = "123456"
                    shopperEmail = "test@adyen.com"
                    isAmountConsentChecked = true
                    isAccountConsentChecked = true
                }

                with(expectMostRecentItem()) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `sort code is empty, then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData {
                    holderName = "test"
                    bankAccountNumber = "12345678"
                    sortCode = ""
                    shopperEmail = "test@adyen.com"
                    isAmountConsentChecked = true
                    isAccountConsentChecked = true
                }

                with(expectMostRecentItem()) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `sort code length more than 6 , then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData {
                    holderName = "test"
                    bankAccountNumber = "12345678"
                    sortCode = "12345678"
                    shopperEmail = "test@adyen.com"
                    isAmountConsentChecked = true
                    isAccountConsentChecked = true
                }

                with(expectMostRecentItem()) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `sort code length less than 6 , then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData {
                    holderName = "test"
                    bankAccountNumber = "12345678"
                    sortCode = "123"
                    shopperEmail = "test@adyen.com"
                    isAmountConsentChecked = true
                    isAccountConsentChecked = true
                }

                with(expectMostRecentItem()) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `shopper email is invalid , then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData {
                    holderName = "test"
                    bankAccountNumber = "12345678"
                    sortCode = "123456"
                    shopperEmail = "test@adyen"
                    isAmountConsentChecked = true
                    isAccountConsentChecked = true
                }

                with(expectMostRecentItem()) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `amount consent check false , then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData {
                    holderName = "test"
                    bankAccountNumber = "12345678"
                    sortCode = "123456"
                    shopperEmail = "test@adyen.com"
                    isAmountConsentChecked = false
                    isAccountConsentChecked = true
                }

                with(expectMostRecentItem()) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `account consent check false , then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData {
                    holderName = "test"
                    bankAccountNumber = "12345678"
                    sortCode = "123456"
                    shopperEmail = "test@adyen.com"
                    isAmountConsentChecked = true
                    isAccountConsentChecked = false
                }

                with(expectMostRecentItem()) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `input data is valid then output should be valid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData {
                    holderName = "test"
                    bankAccountNumber = "12345678"
                    sortCode = "123456"
                    shopperEmail = "test@adyen.com"
                    isAmountConsentChecked = true
                    isAccountConsentChecked = true
                }

                with(expectMostRecentItem()) {
                    assertTrue(isValid)
                }
            }
        }
    }

    @Nested
    @DisplayName("when setMode is called")
    inner class SetModeTest {

        @Test
        fun `with INPUT parameter while current mode is also INPUT, then no value should be emitted`() = runTest {
            delegate.updateInputData { mode = BacsDirectDebitMode.INPUT }

            delegate.viewFlow.test {
                awaitItem()

                delegate.setMode(BacsDirectDebitMode.INPUT)

                expectNoEvents()
            }
        }

        @Test
        fun `with CONFIRMATION parameter while current mode is also CONFIRMATION, then no value should be emitted`() =
            runTest {
                delegate.updateInputData { mode = BacsDirectDebitMode.CONFIRMATION }

                delegate.viewFlow.test {
                    awaitItem()

                    delegate.setMode(BacsDirectDebitMode.CONFIRMATION)

                    expectNoEvents()
                }
            }

        @Test
        fun `with INPUT parameter while current mode is CONFIRMATION, then INPUT should be emitted`() = runTest {
            delegate.updateInputData { mode = BacsDirectDebitMode.CONFIRMATION }

            delegate.viewFlow.test {
                awaitItem()

                delegate.setMode(BacsDirectDebitMode.INPUT)

                assertEquals(BacsComponentViewType.INPUT, expectMostRecentItem())
            }
        }

        @Test
        fun `with CONFIRMATION parameter while current mode is INPUT and input data is valid, then CONFIRMATION should be emitted`() =
            runTest {
                delegate.updateInputData {
                    holderName = "test"
                    bankAccountNumber = "12345678"
                    sortCode = "123456"
                    shopperEmail = "test@adyen.com"
                    isAmountConsentChecked = true
                    isAccountConsentChecked = true
                    mode = BacsDirectDebitMode.INPUT
                }

                delegate.viewFlow.test {
                    awaitItem()

                    delegate.setMode(BacsDirectDebitMode.CONFIRMATION)

                    assertEquals(BacsComponentViewType.CONFIRMATION, expectMostRecentItem())
                }
            }

        @Test
        fun `with CONFIRMATION parameter while current mode is INPUT and input data is invalid, then no value should be emitted`() =
            runTest {
                delegate.updateInputData { mode = BacsDirectDebitMode.INPUT }

                delegate.viewFlow.test {
                    awaitItem()

                    delegate.setMode(BacsDirectDebitMode.CONFIRMATION)

                    expectNoEvents()
                }
            }
    }

    @Nested
    @DisplayName("when creating component state and")
    inner class CreateComponentStateTest {
        @Test
        fun `input is invalid, then component state should be invalid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.updateComponentState(
                    BacsDirectDebitOutputData(
                        holderNameState = FieldState("test", Validation.Invalid(R.string.bacs_holder_name_invalid)),
                        bankAccountNumberState = FieldState(
                            "12345678",
                            Validation.Invalid(R.string.bacs_account_number_invalid),
                        ),
                        sortCodeState = FieldState("123456", Validation.Invalid(R.string.bacs_sort_code_invalid)),
                        shopperEmailState = FieldState("test@adyen.com", Validation.Valid),
                        isAmountConsentChecked = true,
                        isAccountConsentChecked = false,
                        mode = BacsDirectDebitMode.CONFIRMATION,
                    ),
                )

                with(expectMostRecentItem()) {
                    assertFalse(isInputValid)
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `input is valid and mode is INPUT, then component state should be invalid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.updateComponentState(
                    BacsDirectDebitOutputData(
                        holderNameState = FieldState("test", Validation.Valid),
                        bankAccountNumberState = FieldState("12345678", Validation.Valid),
                        sortCodeState = FieldState("123456", Validation.Valid),
                        shopperEmailState = FieldState("test@adyen.com", Validation.Valid),
                        isAmountConsentChecked = true,
                        isAccountConsentChecked = true,
                        mode = BacsDirectDebitMode.INPUT,
                    ),
                )

                with(expectMostRecentItem()) {
                    assertTrue(isInputValid)
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `input is valid and mode is  CONFIRMATION, then component state should be valid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.updateComponentState(
                    BacsDirectDebitOutputData(
                        holderNameState = FieldState("test", Validation.Valid),
                        bankAccountNumberState = FieldState("12345678", Validation.Valid),
                        sortCodeState = FieldState("123456", Validation.Valid),
                        shopperEmailState = FieldState("test@adyen.com", Validation.Valid),
                        isAmountConsentChecked = true,
                        isAccountConsentChecked = true,
                        mode = BacsDirectDebitMode.CONFIRMATION,
                    ),
                )

                with(expectMostRecentItem()) {
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                    assertEquals(TEST_ORDER, data.order)
                }
            }
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.bacs.internal.DefaultBacsDirectDebitDelegateTest#amountSource")
        fun `when input data is valid then amount is propagated in component state if set`(
            configurationValue: Amount?,
            expectedComponentStateValue: Amount?,
        ) = runTest {
            if (configurationValue != null) {
                val configuration = createCheckoutConfiguration(configurationValue)
                delegate = createBacsDelegate(configuration = configuration)
            }
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.componentStateFlow.test {
                delegate.updateInputData {
                    holderName = "test"
                    bankAccountNumber = "12345678"
                    sortCode = "123456"
                    shopperEmail = "test@adyen.com"
                    isAmountConsentChecked = true
                    isAccountConsentChecked = true
                    mode = BacsDirectDebitMode.CONFIRMATION
                }

                assertEquals(expectedComponentStateValue, expectMostRecentItem().data.amount)
            }
        }
    }

    @Nested
    inner class SubmitButtonVisibilityTest {

        @Test
        fun `when submit button is configured to be hidden, then it should not show`() {
            delegate = createBacsDelegate(
                configuration = createCheckoutConfiguration {
                    setSubmitButtonVisible(false)
                },
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `when submit button is configured to be visible, then it should show`() {
            delegate = createBacsDelegate(
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
        fun `when delegate onSubmit is called and component is in input mode and output data is not valid then submit handler onSubmit is called`() =
            runTest {
                delegate.componentStateFlow.test {
                    delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
                    delegate.onSubmit()
                    verify(submitHandler).onSubmit(expectMostRecentItem())
                }
            }

        @Test
        fun `when delegate onSubmit is called and component is in input mode and output data is valid then component should go in confirmation mode`() =
            runTest {
                delegate.outputDataFlow.test {
                    delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                    delegate.updateInputData {
                        holderName = "test"
                        bankAccountNumber = "12345678"
                        sortCode = "123456"
                        shopperEmail = "test@adyen.com"
                        isAmountConsentChecked = true
                        isAccountConsentChecked = true
                    }

                    delegate.onSubmit()

                    assertEquals(BacsDirectDebitMode.CONFIRMATION, expectMostRecentItem().mode)
                }
            }

        @Test
        fun `when delegate onSubmit is called and component is in confirmation mode and output data is valid then submit handler onSubmit is called`() =
            runTest {
                delegate.componentStateFlow.test {
                    delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                    delegate.updateInputData {
                        holderName = "test"
                        bankAccountNumber = "12345678"
                        sortCode = "123456"
                        shopperEmail = "test@adyen.com"
                        isAmountConsentChecked = true
                        isAccountConsentChecked = true
                        mode = BacsDirectDebitMode.CONFIRMATION
                    }

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
            val submitFlow = flow<BacsDirectDebitComponentState> { emit(mock()) }
            whenever(submitHandler.submitFlow) doReturn submitFlow
            val delegate = createBacsDelegate()

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
                    holderName = "test"
                    bankAccountNumber = "12345678"
                    sortCode = "123456"
                    shopperEmail = "test@adyen.com"
                    isAmountConsentChecked = true
                    isAccountConsentChecked = true
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

    private fun createBacsDelegate(
        configuration: CheckoutConfiguration = createCheckoutConfiguration(),
        order: OrderRequest? = TEST_ORDER,
    ) = DefaultBacsDirectDebitDelegate(
        observerRepository = PaymentObserverRepository(),
        componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = Locale.US,
            dropInOverrideParams = null,
            componentSessionParams = null,
            componentConfiguration = configuration.getBacsDirectDebitConfiguration(),
        ),
        paymentMethod = PaymentMethod(type = TEST_PAYMENT_METHOD_TYPE),
        order = order,
        analyticsManager = analyticsManager,
        submitHandler = submitHandler,
        sdkDataProvider = sdkDataProvider,
    )

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: BacsDirectDebitConfiguration.Builder.() -> Unit = {},
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        amount = amount,
    ) {
        bacsDirectDebit(configuration)
    }

    companion object {
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
