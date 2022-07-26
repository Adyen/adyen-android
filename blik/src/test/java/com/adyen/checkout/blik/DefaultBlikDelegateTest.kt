/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 25/7/2022.
 */

package com.adyen.checkout.blik

import app.cash.turbine.test
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultBlikDelegateTest {

    private val delegate = DefaultBlikDelegate(
        paymentMethod = PaymentMethod(),
    )

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `input is invalid, then output data should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                skipItems(1)
                delegate.onInputDataChanged(BlikInputData(blikCode = ""))
                val blikOutputData = awaitItem()

                Assert.assertEquals("", blikOutputData?.blikCodeField?.value)
                Assert.assertEquals(false, blikOutputData?.isValid)

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `input is invalid, then component state should be invalid`() = runTest {
            delegate.componentStateFlow.test {
                skipItems(1)
                delegate.onInputDataChanged(BlikInputData(blikCode = "1234"))
                val componentState = awaitItem()

                Assert.assertEquals("1234", componentState?.data?.paymentMethod?.blikCode)
                Assert.assertEquals(false, componentState?.isInputValid)
                Assert.assertEquals(false, componentState?.isValid)

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `input is valid, then output data should be propagated`() = runTest {
            delegate.outputDataFlow.test {
                skipItems(1)
                delegate.onInputDataChanged(BlikInputData(blikCode = "545897"))
                val blikOutputData = awaitItem()

                Assert.assertEquals("545897", blikOutputData?.blikCodeField?.value)
                Assert.assertEquals(true, blikOutputData?.isValid)

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `input is valid, then component state should be propagated`() = runTest {
            delegate.componentStateFlow.test {
                skipItems(1)
                delegate.onInputDataChanged(BlikInputData(blikCode = "123243"))
                val componentState = awaitItem()

                Assert.assertEquals("123243", componentState?.data?.paymentMethod?.blikCode)
                Assert.assertEquals(true, componentState?.isInputValid)
                Assert.assertEquals(true, componentState?.isValid)

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
                delegate.createComponentState(BlikOutputData("87909090"))
                val componentState = awaitItem()

                Assert.assertEquals("87909090", componentState?.data?.paymentMethod?.blikCode)
                Assert.assertEquals(false, componentState?.isInputValid)
                Assert.assertEquals(false, componentState?.isValid)

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `output data is valid, then component state should be propagated`() = runTest {
            delegate.componentStateFlow.test {
                skipItems(1)
                delegate.createComponentState(BlikOutputData("777134"))
                val componentState = awaitItem()

                Assert.assertEquals("777134", componentState?.data?.paymentMethod?.blikCode)
                Assert.assertEquals(true, componentState?.isInputValid)
                Assert.assertEquals(true, componentState?.isValid)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
