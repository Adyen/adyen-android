/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 18/8/2022.
 */

package com.adyen.checkout.bacs

import app.cash.turbine.test
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultBacsDirectDebitDelegateTest {

    private val delegate = DefaultBacsDirectDebitDelegate(paymentMethod = PaymentMethod())

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `account holder is empty, then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(
                    BacsDirectDebitInputData(
                        holderName = "",
                        bankAccountNumber = "12345678",
                        sortCode = "123456",
                        shopperEmail = "test@adyen.com",
                        isAmountConsentChecked = true,
                        isAccountConsentChecked = true
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `bank account number length more than 8, then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(
                    BacsDirectDebitInputData(
                        holderName = "test",
                        bankAccountNumber = "1234567890",
                        sortCode = "123456",
                        shopperEmail = "test@adyen.com",
                        isAmountConsentChecked = true,
                        isAccountConsentChecked = true
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `bank account number length less than 8, then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(
                    BacsDirectDebitInputData(
                        holderName = "test",
                        bankAccountNumber = "1234",
                        sortCode = "123456",
                        shopperEmail = "test@adyen.com",
                        isAmountConsentChecked = true,
                        isAccountConsentChecked = true
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `bank account number is empty, then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(
                    BacsDirectDebitInputData(
                        holderName = "test",
                        bankAccountNumber = "",
                        sortCode = "123456",
                        shopperEmail = "test@adyen.com",
                        isAmountConsentChecked = true,
                        isAccountConsentChecked = true
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `sort code is empty, then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(
                    BacsDirectDebitInputData(
                        holderName = "test",
                        bankAccountNumber = "12345678",
                        sortCode = "",
                        shopperEmail = "test@adyen.com",
                        isAmountConsentChecked = true,
                        isAccountConsentChecked = true
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `sort code length more than 6 , then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(
                    BacsDirectDebitInputData(
                        holderName = "test",
                        bankAccountNumber = "12345678",
                        sortCode = "12345678",
                        shopperEmail = "test@adyen.com",
                        isAmountConsentChecked = true,
                        isAccountConsentChecked = true
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `sort code length less than 6 , then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(
                    BacsDirectDebitInputData(
                        holderName = "test",
                        bankAccountNumber = "12345678",
                        sortCode = "123",
                        shopperEmail = "test@adyen.com",
                        isAmountConsentChecked = true,
                        isAccountConsentChecked = true
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertFalse(isValid)
                }
            }
        }


        @Test
        fun `shopper email is invalid , then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(
                    BacsDirectDebitInputData(
                        holderName = "test",
                        bankAccountNumber = "12345678",
                        sortCode = "123456",
                        shopperEmail = "test@adyen",
                        isAmountConsentChecked = true,
                        isAccountConsentChecked = true
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `amount consent check false , then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(
                    BacsDirectDebitInputData(
                        holderName = "test",
                        bankAccountNumber = "12345678",
                        sortCode = "123456",
                        shopperEmail = "test@adyen.com",
                        isAmountConsentChecked = false,
                        isAccountConsentChecked = true
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertFalse(isValid)
                }
            }
        }


        @Test
        fun `account consent check false , then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(
                    BacsDirectDebitInputData(
                        holderName = "test",
                        bankAccountNumber = "12345678",
                        sortCode = "123456",
                        shopperEmail = "test@adyen.com",
                        isAmountConsentChecked = true,
                        isAccountConsentChecked = false
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `mode confirmation , then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(
                    BacsDirectDebitInputData(
                        holderName = "test",
                        bankAccountNumber = "12345678",
                        sortCode = "123456",
                        shopperEmail = "test@adyen.com",
                        isAmountConsentChecked = true,
                        isAccountConsentChecked = true
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertTrue(isValid)
                }
            }
        }

        @Test
        fun `input is invalid, them component state should be invalid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.onInputDataChanged(
                    BacsDirectDebitInputData(
                        holderName = "test",
                        bankAccountNumber = "12345678",
                        sortCode = "123456",
                        shopperEmail = "test@adyen.com",
                        isAmountConsentChecked = true,
                        isAccountConsentChecked = false
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `input is valid and mode is INPUT, them component state should be invalid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.onInputDataChanged(
                    BacsDirectDebitInputData(
                        holderName = "test",
                        bankAccountNumber = "12345678",
                        sortCode = "123456",
                        shopperEmail = "test@adyen.com",
                        isAmountConsentChecked = true,
                        isAccountConsentChecked = true
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `input is valid and mode is  CONFIRMATION, them component state should be valid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.onInputDataChanged(
                    BacsDirectDebitInputData(
                        holderName = "test",
                        bankAccountNumber = "12345678",
                        sortCode = "123456",
                        shopperEmail = "test@adyen.com",
                        isAmountConsentChecked = true,
                        isAccountConsentChecked = true,
                        mode = BacsDirectDebitMode.CONFIRMATION
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertTrue(isValid)
                }
            }
        }
    }

    @Nested
    @DisplayName("when creating component state and")
    inner class CreateComponentStateTest {
        @Test
        fun `input is invalid, them component state should be invalid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.onInputDataChanged(
                    BacsDirectDebitInputData(
                        holderName = "test",
                        bankAccountNumber = "12345678",
                        sortCode = "123456",
                        shopperEmail = "test@adyen.com",
                        isAmountConsentChecked = true,
                        isAccountConsentChecked = false
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertFalse(isInputValid)
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `input is valid and mode is INPUT, them component state should be invalid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.onInputDataChanged(
                    BacsDirectDebitInputData(
                        holderName = "test",
                        bankAccountNumber = "12345678",
                        sortCode = "123456",
                        shopperEmail = "test@adyen.com",
                        isAmountConsentChecked = true,
                        isAccountConsentChecked = true
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertTrue(isInputValid)
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `input is valid and mode is  CONFIRMATION, them component state should be valid`() = runTest {
            delegate.componentStateFlow.test {
                delegate.onInputDataChanged(
                    BacsDirectDebitInputData(
                        holderName = "test",
                        bankAccountNumber = "12345678",
                        sortCode = "123456",
                        shopperEmail = "test@adyen.com",
                        isAmountConsentChecked = true,
                        isAccountConsentChecked = true,
                        mode = BacsDirectDebitMode.CONFIRMATION
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertTrue(isInputValid)
                    assertTrue(isValid)
                }
            }
        }
    }
}
