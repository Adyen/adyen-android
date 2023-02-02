/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 28/12/2022.
 */

package com.adyen.checkout.dropin.ui.viewmodel

import app.cash.turbine.test
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.ui.paymentmethods.GenericStoredModel
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class PreselectedStoredPaymentViewModelTest {
    private val storedPaymentMethod = StoredPaymentMethod()
    private val dropInConfiguration = DropInConfiguration.Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY).build()

    private lateinit var viewModel: PreselectedStoredPaymentViewModel

    @BeforeEach
    fun setup() {
        viewModel = PreselectedStoredPaymentViewModel(
            storedPaymentMethod,
            TEST_AMOUNT,
            dropInConfiguration,
        )
    }

    @Test
    fun `when view model is initialized then uiStateFlow has a matching initial value`() = runTest {
        val dropInConfiguration = DropInConfiguration.Builder(
            Locale.US,
            Environment.TEST,
            TEST_CLIENT_KEY
        )
            .setEnableRemovingStoredPaymentMethods(true)
            .build()

        viewModel = PreselectedStoredPaymentViewModel(
            storedPaymentMethod,
            TEST_AMOUNT,
            dropInConfiguration,
        )

        viewModel.uiStateFlow.test {
            val actual = awaitItem()

            with(actual.storedPaymentMethodModel) {
                this as? GenericStoredModel
                    ?: fail("Expected: GenericStoredModel, Actual: ${this::class.java.simpleName}")
                assertTrue(isRemovable)
                assertEquals(Environment.TEST, environment)
            }

            assertEquals(ButtonState.ContinueButton(), actual.buttonState)
        }
    }

    @Test
    fun `when component emits a new state with invalid input then button state should be continue button`() =
        runTest {
            viewModel.uiStateFlow.test {
                val componentState = PaymentComponentState(PaymentComponentData(), isInputValid = false, isReady = true)
                viewModel.onStateChanged(componentState)

                assertEquals(ButtonState.ContinueButton(), awaitItem().buttonState)
            }
        }

    @Test
    fun `when component emits a new state with valid input then button state should be pay button`() =
        runTest {
            viewModel.uiStateFlow.test {
                val componentState = PaymentComponentState(PaymentComponentData(), isInputValid = true, isReady = true)
                viewModel.onStateChanged(componentState)

                assertEquals(ButtonState.PayButton(TEST_AMOUNT, Locale.US), expectMostRecentItem().buttonState)
            }
        }

    @Test
    fun `when component emits an error then view model should propagate this error`() = runTest {
        viewModel.eventsFlow.test {
            val componentError = ComponentError(CheckoutException("Test message", Exception("Test exception")))
            viewModel.onError(componentError)

            assertEquals(PreselectedStoredEvent.ShowError(componentError), awaitItem())
        }
    }

    @Test
    fun `when component emits a submit event then view model should emit to request a payments call`() = runTest {
        viewModel.eventsFlow.test {
            val componentState = PaymentComponentState(PaymentComponentData(), isInputValid = true, isReady = true)
            viewModel.onSubmit(componentState)

            assertEquals(PreselectedStoredEvent.RequestPaymentsCall(componentState), awaitItem())
        }
    }

    @Test
    fun `when component emits an action event then view model should throw an exception`() = runTest {
        viewModel.eventsFlow.test {
            assertThrows<IllegalStateException> {
                viewModel.onAdditionalDetails(ActionComponentData())
            }
        }
    }

    @Test
    fun `when button is clicked with an invalid input then view model should request showing the stored component in a new screen`() =
        runTest {
            viewModel.eventsFlow.test {
                val componentState = PaymentComponentState(PaymentComponentData(), isInputValid = false, isReady = true)
                viewModel.onStateChanged(componentState)
                viewModel.onButtonClicked()

                assertEquals(PreselectedStoredEvent.ShowStoredPaymentScreen, awaitItem())
            }
        }

    @Test
    fun `when button is clicked with a valid input then view model should request submitting the component`() =
        runTest {
            viewModel.eventsFlow.test {
                val componentState = PaymentComponentState(PaymentComponentData(), isInputValid = true, isReady = true)
                viewModel.onStateChanged(componentState)
                viewModel.onButtonClicked()

                assertEquals(PreselectedStoredEvent.SubmitComponent, awaitItem())
            }
        }

    @Test
    fun `when button is clicked with a valid input but non ready state then button state should become loading`() =
        runTest {
            viewModel.uiStateFlow.test {
                val componentState = PaymentComponentState(PaymentComponentData(), isInputValid = true, isReady = false)
                viewModel.onStateChanged(componentState)
                viewModel.onButtonClicked()

                assertEquals(ButtonState.Loading, expectMostRecentItem().buttonState)
            }
        }

    @Test
    fun `when button is clicked with a valid input and a ready state then button state should remain pay button`() =
        runTest {
            viewModel.uiStateFlow.test {
                val componentState = PaymentComponentState(PaymentComponentData(), isInputValid = true, isReady = true)
                viewModel.onStateChanged(componentState)
                viewModel.onButtonClicked()

                assertEquals(ButtonState.PayButton(TEST_AMOUNT, Locale.US), expectMostRecentItem().buttonState)
            }
        }

    companion object {
        private val TEST_AMOUNT = Amount("USD", 1337)
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
