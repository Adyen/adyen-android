/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 17/8/2022.
 */

package com.adyen.checkout.issuerlist

import app.cash.turbine.test
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.issuerlist.utils.TestIssuerPaymentMethod
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
class DefaultIssuerListDelegateTest {

    private val delegate = DefaultIssuerListDelegate(
        paymentMethod = PaymentMethod(),
        typedPaymentMethodFactory = { TestIssuerPaymentMethod() }
    )

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `selectedIssuer is null, then output should be null`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(IssuerListInputData(null))
                val outputData = expectMostRecentItem()
                Assert.assertNull(outputData?.selectedIssuer)
            }
        }

        @Test
        fun `selectedIssuer is null, then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(IssuerListInputData(null))
                val outputData = expectMostRecentItem()
                Assert.assertNull(outputData?.selectedIssuer)
                Assert.assertEquals(false, outputData?.isValid == true)
            }
        }

        @Test
        fun `selectedIssuer is valid, then output should be valid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(IssuerListInputData(IssuerModel(id = "id", name = "test")))
                val outputData = expectMostRecentItem()
                Assert.assertEquals("test", outputData?.selectedIssuer?.name)
                Assert.assertEquals("id", outputData?.selectedIssuer?.id)
                Assert.assertEquals(true, outputData?.isValid == true)
            }
        }

        @Test
        fun `selectedIssuer is null, then component state should be invalid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.onInputDataChanged(IssuerListInputData())
                val componentState = expectMostRecentItem()
                Assert.assertEquals("", componentState?.data?.paymentMethod?.issuer)
                Assert.assertEquals(false, componentState?.isValid)
            }
        }

        @Test
        fun `selectIssuer is valid, then component state should be valid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.onInputDataChanged(
                    IssuerListInputData(selectedIssuer = IssuerModel(id = "issuer-id", name = "issuer-name"))
                )
                val componentState = expectMostRecentItem()
                Assert.assertEquals("issuer-id", componentState?.data?.paymentMethod?.issuer)
                Assert.assertEquals(true, componentState?.isValid)
            }
        }
    }

    @Nested
    @DisplayName("when creating component state and")
    inner class CreateComponentStateTest {

        @Test
        fun `output is invalid, then component state should be invalid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.createComponentState(IssuerListOutputData(null))
                val componentState = expectMostRecentItem()
                Assert.assertEquals(false, componentState?.isInputValid)
                Assert.assertEquals(false, componentState?.isValid)
            }
        }

        @Test
        fun `output is valid, then component state should be valid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.createComponentState(IssuerListOutputData(IssuerModel(id = "issuer-id", name = "issuer-name")))
                val componentState = expectMostRecentItem()
                Assert.assertEquals("issuer-id", componentState?.data?.paymentMethod?.issuer)
                Assert.assertEquals(true, componentState?.isInputValid)
                Assert.assertEquals(true, componentState?.isValid)
            }
        }
    }

}
