/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 14/9/2022.
 */

package com.adyen.checkout.onlinebankingcz

import app.cash.turbine.test
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.OnlineBankingCZPaymentMethod
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
internal class DefaultOnlineBankingCZDelegateTest {

    private val delegate = DefaultOnlineBankingCZDelegate(
        paymentMethod = PaymentMethod(),
        paymentMethodFactory = { OnlineBankingCZPaymentMethod() }
    )

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `selectedIssuer is null, then output should be null`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(OnlineBankingInputData(null))
                with(requireNotNull(expectMostRecentItem())) {
                    Assert.assertNull(selectedIssuer)
                }
            }
        }

        @Test
        fun `selectedIssuer is null, then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(OnlineBankingInputData(null))
                with(requireNotNull(expectMostRecentItem())) {
                    Assert.assertNull(selectedIssuer)
                    Assert.assertFalse(isValid)
                }
            }
        }

        @Test
        fun `selectedIssuer is valid, then output should be valid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(OnlineBankingInputData(OnlineBankingModel(id = "id", name = "test")))
                with(requireNotNull(expectMostRecentItem())) {
                    Assert.assertEquals("test", selectedIssuer?.name)
                    Assert.assertEquals("id", selectedIssuer?.id)
                    Assert.assertTrue(isValid)
                }
            }
        }

        @Test
        fun `selectedIssuer is null, then component state should be invalid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.onInputDataChanged(OnlineBankingInputData())
                with(requireNotNull(expectMostRecentItem())) {
                    Assert.assertEquals("", data.paymentMethod?.issuer)
                    Assert.assertFalse(isValid)
                }
            }
        }

        @Test
        fun `selectIssuer is valid, then component state should be valid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.onInputDataChanged(
                    OnlineBankingInputData(selectedIssuer = OnlineBankingModel(id = "issuer-id", name = "issuer-name"))
                )
                with(requireNotNull(expectMostRecentItem())) {
                    Assert.assertEquals("issuer-id", data.paymentMethod?.issuer)
                    Assert.assertTrue(isValid)
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
                delegate.createComponentState(OnlineBankingOutputData(null))
                with(requireNotNull(expectMostRecentItem())) {
                    Assert.assertFalse(isInputValid)
                    Assert.assertFalse(isValid)
                }
            }
        }

        @Test
        fun `output is valid, then component state should be valid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.createComponentState(
                    OnlineBankingOutputData(
                        OnlineBankingModel(id = "issuer-id", name = "issuer-name")
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    Assert.assertEquals("issuer-id", data.paymentMethod?.issuer)
                    Assert.assertTrue(isInputValid)
                    Assert.assertTrue(isValid)
                }
            }
        }
    }
}
