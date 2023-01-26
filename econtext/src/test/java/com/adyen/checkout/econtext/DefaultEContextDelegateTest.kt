/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 25/1/2023.
 */

package com.adyen.checkout.econtext

import app.cash.turbine.test
import com.adyen.checkout.components.analytics.AnalyticsRepository
import com.adyen.checkout.components.base.ButtonComponentParamsMapper
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.SubmitHandler
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.log.Logger
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
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultEContextDelegateTest(
    @Mock private val analyticsRepository: AnalyticsRepository,
) {

    private lateinit var delegate: DefaultEContextDelegate<*>

    @BeforeEach
    fun beforeEach() {
        delegate = createEContextDelegate()
        Logger.setLogcatLevel(Logger.NONE)
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
                    )
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
                    )
                )
                with(expectMostRecentItem()) {
                    assertEquals("firstName", data.paymentMethod?.firstName)
                    assertEquals("lastName", data.paymentMethod?.lastName)
                    assertEquals("phoneNumber", data.paymentMethod?.telephoneNumber)
                    assertEquals("emailAddress", data.paymentMethod?.shopperEmail)
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                }
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
            delegate = createEContextDelegate(
                configuration = getDefaultTestEContextConfigurationBuilder()
                    .setSubmitButtonVisible(false)
                    .build()
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `when submit button is configured to be visible, then it should show`() {
            delegate = createEContextDelegate(
                configuration = getDefaultTestEContextConfigurationBuilder()
                    .setSubmitButtonVisible(true)
                    .build()
            )

            assertTrue(delegate.shouldShowSubmitButton())
        }
    }

    private fun createEContextDelegate(
        configuration: TestEContextConfiguration = getDefaultTestEContextConfigurationBuilder().build()
    ) = DefaultEContextDelegate(
        observerRepository = PaymentObserverRepository(),
        componentParams = ButtonComponentParamsMapper(null).mapToParams(configuration),
        paymentMethod = PaymentMethod(),
        analyticsRepository = analyticsRepository,
        submitHandler = SubmitHandler()
    ) { TestEContextPaymentMethod() }

    private fun getDefaultTestEContextConfigurationBuilder() = TestEContextConfiguration.Builder(
        Locale.US,
        Environment.TEST,
        TEST_CLIENT_KEY_1
    )

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
