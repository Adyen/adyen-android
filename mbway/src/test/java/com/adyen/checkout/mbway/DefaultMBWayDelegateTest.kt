/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 25/7/2022.
 */

package com.adyen.checkout.mbway

import app.cash.turbine.test
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultMBWayDelegateTest {

    private val delegate = DefaultMBWayDelegate(
        paymentMethod = PaymentMethod(),
    )

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `input is invalid, then output data should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                skipItems(1)
                delegate.onInputDataChanged(MBWayInputData(countryCode = "+1", localPhoneNumber = "04023456"))

                with(requireNotNull(awaitItem())) {
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
                delegate.onInputDataChanged(MBWayInputData(countryCode = "+23", localPhoneNumber = "0056778"))

                with(requireNotNull(awaitItem())) {
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
                delegate.onInputDataChanged(MBWayInputData(countryCode = "+351", localPhoneNumber = "234567890"))

                with(requireNotNull(awaitItem())) {
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
                delegate.onInputDataChanged(MBWayInputData(countryCode = "+1", localPhoneNumber = "9257348920"))

                with(requireNotNull(awaitItem())) {
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
                delegate.createComponentState(MBWayOutputData("+7867676"))

                with(requireNotNull(awaitItem())) {
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
                delegate.createComponentState(MBWayOutputData("+31666666666"))

                with(requireNotNull(awaitItem())) {
                    assertEquals("+31666666666", data.paymentMethod?.telephoneNumber)
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
