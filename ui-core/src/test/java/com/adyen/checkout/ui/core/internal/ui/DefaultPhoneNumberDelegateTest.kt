/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/3/2023.
 */

package com.adyen.checkout.ui.core.internal.ui

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultPhoneNumberDelegateTest {

    private lateinit var delegate: DefaultPhoneNumberDelegate

    @BeforeEach
    fun beforeEach() {
        delegate = DefaultPhoneNumberDelegate()
    }

    @Test
    fun `when input data changes, then change listener should be called`() {
        val callback: () -> Unit = mock()
        delegate.onInputDataChangedListener = callback

        delegate.updatePhoneNumberInputData { }

        verify(callback).invoke()
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `input is invalid, then output data should be invalid`() = runTest {
            delegate.phoneNumberOutputDataFlow.test {
                skipItems(1)
                delegate.updatePhoneNumberInputData {
                    countryCode = "+1"
                    everythingAfterCountryCode = "04023456"
                }

                with(awaitItem()) {
                    assertEquals("+14023456", phoneNumber.value)
                    assertFalse(isValid)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `input is valid, then output data should be propagated`() = runTest {
            delegate.phoneNumberOutputDataFlow.test {
                skipItems(1)
                delegate.updatePhoneNumberInputData {
                    countryCode = "+351"
                    everythingAfterCountryCode = "234567890"
                }

                with(awaitItem()) {
                    assertEquals("+351234567890", phoneNumber.value)
                    assertTrue(isValid)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
