/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 22/8/2022.
 */

package com.adyen.checkout.bcmc

import app.cash.turbine.test
import com.adyen.checkout.card.CardValidationMapper
import com.adyen.checkout.card.R
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.test.TestPublicKeyRepository
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.cse.test.TestCardEncrypter
import com.adyen.checkout.test.TestDispatcherExtension
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
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class DefaultBcmcDelegateTest {

    private lateinit var testPublicKeyRepository: TestPublicKeyRepository
    private lateinit var cardEncrypter: TestCardEncrypter
    private lateinit var configuration: BcmcConfiguration
    private lateinit var cardValidationMapper: CardValidationMapper
    private lateinit var delegate: DefaultBcmcDelegate

    @BeforeEach
    fun setup() {
        testPublicKeyRepository = TestPublicKeyRepository()
        cardEncrypter = TestCardEncrypter()
        configuration = BcmcConfiguration.Builder(
            Locale.US,
            Environment.TEST,
            TEST_CLIENT_KEY
        ).build()
        cardValidationMapper = CardValidationMapper()
        delegate = DefaultBcmcDelegate(
            paymentMethod = PaymentMethod(),
            publicKeyRepository = testPublicKeyRepository,
            configuration = configuration,
            cardValidationMapper = cardValidationMapper,
            cardEncrypter = cardEncrypter
        )
    }

    @Test
    fun `when fetching the public key fails, then an error is propagated`() = runTest {
        testPublicKeyRepository.shouldReturnError = true

        delegate.exceptionFlow.test {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val exception = expectMostRecentItem()
            assertEquals(testPublicKeyRepository.errorResult.exceptionOrNull(), exception.cause)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {
        @Test
        fun `card number is empty, then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(BcmcInputData(cardNumber = "", expiryDate = TEST_EXPIRY_DATE))

                with(requireNotNull(expectMostRecentItem())) {
                    assertTrue(cardNumberField.validation is Validation.Invalid)
                    assertTrue(expiryDateField.validation is Validation.Valid)
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `card number is invalid, then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(BcmcInputData(cardNumber = "12345678", expiryDate = TEST_EXPIRY_DATE))

                with(requireNotNull(expectMostRecentItem())) {
                    assertTrue(cardNumberField.validation is Validation.Invalid)
                    assertTrue(expiryDateField.validation is Validation.Valid)
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `expiry date is invalid, then output should be invalid`() =
            runTest {
                delegate.outputDataFlow.test {
                    delegate.onInputDataChanged(
                        BcmcInputData(
                            cardNumber = TEST_CARD_NUMBER,
                            expiryDate = ExpiryDate.INVALID_DATE
                        )
                    )
                    with(requireNotNull(expectMostRecentItem())) {
                        assertTrue(cardNumberField.validation is Validation.Valid)
                        assertTrue(expiryDateField.validation is Validation.Invalid)
                        assertFalse(isValid)
                    }
                }
            }

        @Test
        fun `expiry date is empty, then output should be invalid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(
                    BcmcInputData(
                        cardNumber = TEST_CARD_NUMBER,
                        expiryDate = ExpiryDate.EMPTY_DATE
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertTrue(cardNumberField.validation is Validation.Valid)
                    assertTrue(expiryDateField.validation is Validation.Invalid)
                    assertFalse(isValid)
                }
            }
        }

        @Test
        fun `input is valid, then output should be valid`() = runTest {
            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(
                    BcmcInputData(
                        cardNumber = TEST_CARD_NUMBER,
                        expiryDate = TEST_EXPIRY_DATE
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertTrue(cardNumberField.validation is Validation.Valid)
                    assertTrue(expiryDateField.validation is Validation.Valid)
                    assertTrue(isValid)
                }
            }
        }


    }

    @Nested
    @DisplayName("when creating component state and")
    inner class CreateComponentStateTest {

        @Test
        fun `component is not initialized, then component state should not be ready`() = runTest {
            delegate.componentStateFlow.test {
                delegate.createComponentState(
                    createOutputData(
                        cardNumber = FieldState(TEST_CARD_NUMBER, Validation.Valid),
                        expiryDate = FieldState(TEST_EXPIRY_DATE, Validation.Valid)
                    )
                )

                with(requireNotNull(expectMostRecentItem())) {
                    assertFalse(isReady)
                }
            }
        }

        @Test
        fun `encryption fails, then component state should be invalid`() = runTest {
            cardEncrypter.shouldThrowException = true

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.createComponentState(
                    createOutputData(
                        cardNumber = FieldState(TEST_CARD_NUMBER, Validation.Valid),
                        expiryDate = FieldState(TEST_EXPIRY_DATE, Validation.Valid)
                    )
                )

                with(requireNotNull(expectMostRecentItem())) {
                    assertTrue(isReady)
                    assertFalse(isInputValid)
                }
            }
        }

        @Test
        fun `card number in output data is invalid, then component state should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.componentStateFlow.test {
                delegate.createComponentState(
                    createOutputData(
                        cardNumber = FieldState(
                            "12345678", Validation.Invalid(R.string.checkout_card_number_not_valid)
                        ),
                        expiryDate = FieldState(TEST_EXPIRY_DATE, Validation.Valid),
                    )
                )

                with(requireNotNull(expectMostRecentItem())) {
                    assertFalse(isValid)
                    assertFalse(isInputValid)
                }
            }
        }

        @Test
        fun `expiry date in output is invalid, then component state should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.componentStateFlow.test {
                delegate.createComponentState(
                    createOutputData(
                        cardNumber = FieldState(TEST_CARD_NUMBER, Validation.Valid),
                        expiryDate = FieldState(
                            ExpiryDate.INVALID_DATE,
                            Validation.Invalid(R.string.checkout_expiry_date_not_valid)
                        )
                    )
                )

                with(requireNotNull(expectMostRecentItem())) {
                    assertFalse(isValid)
                    assertFalse(isInputValid)
                }
            }
        }

        @Test
        fun `output data is valid, then component state should be valid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.componentStateFlow.test {
                delegate.createComponentState(
                    createOutputData(
                        cardNumber = FieldState(TEST_CARD_NUMBER, Validation.Valid),
                        expiryDate = FieldState(TEST_EXPIRY_DATE, Validation.Valid)
                    )
                )
                with(requireNotNull(expectMostRecentItem())) {
                    assertTrue(isValid)
                    assertTrue(isInputValid)
                }
            }
        }
    }

    private fun createOutputData(
        cardNumber: FieldState<String>,
        expiryDate: FieldState<ExpiryDate>,
        isStoredPaymentMethodEnable: Boolean = false
    ): BcmcOutputData {
        return BcmcOutputData(
            cardNumber,
            expiryDate,
            isStoredPaymentMethodEnable
        )
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CARD_NUMBER = "5555444433331111"
        private val TEST_EXPIRY_DATE = ExpiryDate(3, 2030)
    }
}
