/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 25/1/2023.
 */

package com.adyen.checkout.econtext.internal.ui

import app.cash.turbine.test
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.AdyenLogger
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.econtext.TestEContextComponentState
import com.adyen.checkout.econtext.TestEContextConfiguration
import com.adyen.checkout.econtext.TestEContextPaymentMethod
import com.adyen.checkout.econtext.internal.ui.model.EContextOutputData
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
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultEContextDelegateTest(
    @Mock private val analyticsRepository: AnalyticsRepository,
    @Mock private val submitHandler: SubmitHandler<TestEContextComponentState>,
) {

    private lateinit var delegate: DefaultEContextDelegate<TestEContextPaymentMethod, TestEContextComponentState>

    @BeforeEach
    fun beforeEach() {
        delegate = createEContextDelegate()
        AdyenLogger.setLogLevel(Logger.NONE)
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `input data fields are empty, then output should be empty`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData {
                    firstName = ""
                    lastName = ""
                    mobileNumber = ""
                    countryCode = ""
                    emailAddress = ""
                }

                with(expectMostRecentItem()) {
                    assertEquals("", firstNameState.value)
                    assertEquals("", lastNameState.value)
                    assertEquals("", phoneNumberState.value)
                    assertEquals("", emailAddressState.value)
                }
            }
        }

        @Test
        fun `input data is not valid, then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData {
                    firstName = ""
                    lastName = ""
                    mobileNumber = ""
                    countryCode = ""
                    emailAddress = ""
                }

                with(expectMostRecentItem()) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `input data is valid, then output should be valid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.updateInputData {
                    firstName = "firstName"
                    lastName = "lastName"
                    mobileNumber = "12345678"
                    countryCode = "+31"
                    emailAddress = "abc@mail.com"
                }

                with(expectMostRecentItem()) {
                    assertEquals("firstName", firstNameState.value)
                    assertEquals("lastName", lastNameState.value)
                    assertEquals("+3112345678", phoneNumberState.value)
                    assertEquals("abc@mail.com", emailAddressState.value)
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
                delegate.updateComponentState(
                    EContextOutputData(
                        firstNameState = FieldState("", Validation.Invalid(0)),
                        lastNameState = FieldState("", Validation.Invalid(0)),
                        phoneNumberState = FieldState("", Validation.Invalid(0)),
                        emailAddressState = FieldState("", Validation.Invalid(0)),
                    ),
                )
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
                    EContextOutputData(
                        firstNameState = FieldState("firstName", Validation.Valid),
                        lastNameState = FieldState("lastName", Validation.Valid),
                        phoneNumberState = FieldState("phoneNumber", Validation.Valid),
                        emailAddressState = FieldState("emailAddress", Validation.Valid),
                    ),
                )
                with(expectMostRecentItem()) {
                    with(requireNotNull(data.paymentMethod)) {
                        assertEquals("firstName", firstName)
                        assertEquals("lastName", lastName)
                        assertEquals("phoneNumber", telephoneNumber)
                        assertEquals("emailAddress", shopperEmail)
                    }
                    assertEquals(TEST_ORDER, data.order)
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                }
            }
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.econtext.internal.ui.DefaultEContextDelegateTest#amountSource")
        fun `when input data is valid then amount is propagated in component state if set`(
            configurationValue: Amount?,
            expectedComponentStateValue: Amount?,
        ) = runTest {
            if (configurationValue != null) {
                val configuration = createCheckoutConfiguration(configurationValue)
                delegate = createEContextDelegate(configuration = configuration)
            }
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.componentStateFlow.test {
                delegate.updateInputData {
                    firstName = "firstName"
                    lastName = "lastName"
                    mobileNumber = "12345678"
                    countryCode = "+31"
                    emailAddress = "abc@mail.com"
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
            delegate = createEContextDelegate(
                configuration = createCheckoutConfiguration {
                    setSubmitButtonVisible(false)
                },
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `when submit button is configured to be visible, then it should show`() {
            delegate = createEContextDelegate(
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
                    firstName = "firstName"
                    lastName = "lastName"
                    mobileNumber = "12345678"
                    countryCode = "+31"
                    emailAddress = "abc@mail.com"
                }

                assertEquals(TEST_CHECKOUT_ATTEMPT_ID, expectMostRecentItem().data.paymentMethod?.checkoutAttemptId)
            }
        }
    }

    private fun createEContextDelegate(
        configuration: CheckoutConfiguration = createCheckoutConfiguration(),
        order: Order = TEST_ORDER
    ) = DefaultEContextDelegate(
        observerRepository = PaymentObserverRepository(),
        componentParams = ButtonComponentParamsMapper(false, null).mapToParams(
            checkoutConfiguration = configuration,
            configuration = configuration.getConfiguration(
                TEST_CONFIGURATION_KEY,
            ),
            sessionParams = null,
        ),
        paymentMethod = PaymentMethod(),
        order = order,
        analyticsRepository = analyticsRepository,
        submitHandler = submitHandler,
        typedPaymentMethodFactory = { TestEContextPaymentMethod() },
        componentStateFactory = { data, isInputValid, isReady ->
            TestEContextComponentState(
                data = data,
                isInputValid = isInputValid,
                isReady = isReady,
            )
        },
    )

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: TestEContextConfiguration.Builder.() -> Unit = {}
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
        amount = amount,
    ) {
        val econtextConfiguration = TestEContextConfiguration.Builder(shopperLocale, environment, clientKey)
            .apply(configuration)
            .build()
        addConfiguration(TEST_CONFIGURATION_KEY, econtextConfiguration)
    }

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
        private const val TEST_CHECKOUT_ATTEMPT_ID = "TEST_CHECKOUT_ATTEMPT_ID"
        private const val TEST_CONFIGURATION_KEY = "TEST_CONFIGURATION_KEY"

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
