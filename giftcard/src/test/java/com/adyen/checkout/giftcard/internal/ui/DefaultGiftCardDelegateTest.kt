/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/7/2022.
 */

package com.adyen.checkout.giftcard.internal.ui

import app.cash.turbine.test
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.test.TestPublicKeyRepository
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.Environment
import com.adyen.checkout.cse.internal.test.TestCardEncryptor
import com.adyen.checkout.giftcard.GiftCardAction
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.giftcard.GiftCardConfiguration
import com.adyen.checkout.giftcard.GiftCardException
import com.adyen.checkout.giftcard.giftCard
import com.adyen.checkout.giftcard.internal.ui.model.GiftCardComponentParamsMapper
import com.adyen.checkout.giftcard.internal.ui.model.GiftCardOutputData
import com.adyen.checkout.giftcard.internal.util.DefaultGiftCardValidator
import com.adyen.checkout.giftcard.internal.util.GiftCardBalanceStatus
import com.adyen.checkout.giftcard.internal.util.GiftCardNumberUtils
import com.adyen.checkout.giftcard.internal.util.GiftCardPinUtils
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.test
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.ui.core.internal.ui.model.ExpiryDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
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
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class DefaultGiftCardDelegateTest(
    @Mock private val submitHandler: SubmitHandler<GiftCardComponentState>,
) {

    private lateinit var cardEncryptor: TestCardEncryptor
    private lateinit var publicKeyRepository: TestPublicKeyRepository
    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var delegate: DefaultGiftCardDelegate

    @BeforeEach
    fun before() {
        cardEncryptor = TestCardEncryptor()
        publicKeyRepository = TestPublicKeyRepository()
        analyticsManager = TestAnalyticsManager()
        delegate = createGiftCardDelegate()
    }

    @Test
    fun `when fetching the public key fails, then an error is propagated`() = runTest {
        publicKeyRepository.shouldReturnError = true

        delegate.exceptionFlow.test {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val exception = expectMostRecentItem()

            assertEquals(publicKeyRepository.errorResult.exceptionOrNull(), exception.cause)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Nested
    @DisplayName("when creating component state and")
    inner class CreateComponentStateTest {

        @Test
        fun `public key is null, then component state should not be ready`() = runTest {
            delegate.componentStateFlow.test {
                delegate.updateComponentState(giftCardOutputDataWith("5555444433330000", "737"))

                val componentState = expectMostRecentItem()

                assertFalse(componentState.isReady)
                assertEquals(null, componentState.lastFourDigits)
                assertEquals(GiftCardAction.Idle, componentState.giftCardAction)
            }
        }

        @Test
        fun `output data is invalid, then component state should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateComponentState(giftCardOutputDataWith("123", "737"))

                val componentState = expectMostRecentItem()

                assertTrue(componentState.isReady)
                assertFalse(componentState.isInputValid)
                assertEquals(null, componentState.lastFourDigits)
                assertEquals(GiftCardAction.Idle, componentState.giftCardAction)
            }
        }

        @Test
        fun `encryption fails, then component state should be invalid`() = runTest {
            cardEncryptor.shouldThrowException = true

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateComponentState(giftCardOutputDataWith("5555444433330000", "737"))

                val componentState = expectMostRecentItem()

                assertTrue(componentState.isReady)
                assertFalse(componentState.isInputValid)
                assertEquals(null, componentState.lastFourDigits)
                assertEquals(GiftCardAction.Idle, componentState.giftCardAction)
            }
        }

        @Test
        fun `everything is valid, then component state should be good`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateComponentState(giftCardOutputDataWith("5555444433330000", "737"))

                val componentState = expectMostRecentItem()

                assertNotNull(componentState.data.paymentMethod)
                assertTrue(componentState.isInputValid)
                assertTrue(componentState.isReady)
                assertEquals("0000", componentState.lastFourDigits)
                assertEquals(TEST_ORDER, componentState.data.order)
                assertEquals(GiftCardAction.CheckBalance, componentState.giftCardAction)
            }
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.giftcard.internal.ui.DefaultGiftCardDelegateTest#amountSource")
        fun `when input data is valid then amount is propagated in component state if set`(
            configurationValue: Amount?,
            expectedComponentStateValue: Amount?,
        ) = runTest {
            if (configurationValue != null) {
                val configuration = createCheckoutConfiguration(configurationValue)
                delegate = createGiftCardDelegate(configuration = configuration)
            }
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.componentStateFlow.test {
                delegate.updateInputData {
                    cardNumber = "5555444433330000"
                    pin = "737"
                }
                assertEquals(expectedComponentStateValue, expectMostRecentItem().data.amount)
            }
        }
    }

    @Nested
    inner class SubmitButtonVisibilityTest {

        @Test
        fun `when submit button is configured to be hidden, then it should not show`() {
            delegate = createGiftCardDelegate(
                configuration = createCheckoutConfiguration {
                    setSubmitButtonVisible(false)
                },
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `when submit button is configured to be visible, then it should show`() {
            delegate = createGiftCardDelegate(
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
    inner class BalanceStatusTest {

        @Test
        fun `when delegate is initialized then initial giftCardAction should be Idle`() = runTest {
            delegate.initialize(CoroutineScope((UnconfinedTestDispatcher())))
            delegate.componentStateFlow.test {
                val state = expectMostRecentItem()
                assertEquals(GiftCardAction.Idle, state.giftCardAction)
            }
        }

        @Test
        fun `when balance result is FullPayment then giftCardAction should be SendPayment`() = runTest {
            val fullPaymentBalanceStatus = GiftCardBalanceStatus.FullPayment(
                amountPaid = Amount(value = 50_00, currency = "EUR"),
                remainingBalance = Amount(value = 0L, currency = "EUR"),
            )
            delegate.resolveBalanceStatus(fullPaymentBalanceStatus)

            delegate.componentStateFlow.test {
                val state = expectMostRecentItem()

                verify(submitHandler).onSubmit(state)
                assertEquals(GiftCardAction.SendPayment, state.giftCardAction)
            }
        }

        @Test
        fun `when balance result is PartialPayment and order is null then giftCardAction should be CreateOrder`() =
            runTest {
                delegate = createGiftCardDelegate(order = null)
                val partialPaymentBalanceStatus = GiftCardBalanceStatus.PartialPayment(
                    amountPaid = Amount(value = 50_00, currency = "EUR"),
                    remainingBalance = Amount(value = 0L, currency = "EUR"),
                )
                delegate.resolveBalanceStatus(partialPaymentBalanceStatus)

                delegate.componentStateFlow.test {
                    val state = expectMostRecentItem()

                    verify(submitHandler).onSubmit(state)
                    assertEquals(GiftCardAction.CreateOrder, state.giftCardAction)
                }
            }

        @Test
        fun `when balance result is PartialPayment and order is not null then giftCardAction should be SendPayment`() =
            runTest {
                val partialPaymentBalanceStatus = GiftCardBalanceStatus.PartialPayment(
                    amountPaid = Amount(value = 50_00, currency = "EUR"),
                    remainingBalance = Amount(value = 0L, currency = "EUR"),
                )
                delegate.resolveBalanceStatus(partialPaymentBalanceStatus)

                delegate.componentStateFlow.test {
                    val state = expectMostRecentItem()

                    verify(submitHandler).onSubmit(state)
                    assertEquals(GiftCardAction.SendPayment, state.giftCardAction)
                }
            }

        @Test
        fun `when balance result is NonMatchingCurrencies then an exception should be thrown`() = runTest {
            val nonMatchingCurrenciesBalanceStatus = GiftCardBalanceStatus.NonMatchingCurrencies
            delegate.resolveBalanceStatus(nonMatchingCurrenciesBalanceStatus)

            delegate.exceptionFlow.test {
                val exception = expectMostRecentItem()
                assert(exception is GiftCardException)
            }
        }

        @Test
        fun `when balance result is ZeroAmountToBePaid then an exception should be thrown`() = runTest {
            val zeroAmountToBePaidBalanceStatus = GiftCardBalanceStatus.ZeroAmountToBePaid
            delegate.resolveBalanceStatus(zeroAmountToBePaidBalanceStatus)

            delegate.exceptionFlow.test {
                val exception = expectMostRecentItem()
                assert(exception is GiftCardException)
            }
        }

        @Test
        fun `when balance result is ZeroBalance then an exception should be thrown`() = runTest {
            val zeroBalanceStatus = GiftCardBalanceStatus.ZeroBalance
            delegate.resolveBalanceStatus(zeroBalanceStatus)

            delegate.exceptionFlow.test {
                val exception = expectMostRecentItem()
                assert(exception is GiftCardException)
            }
        }
    }

    @Test
    fun `when resolveOrderResponse is called giftCardAction should be SendPayment`() = runTest {
        val orderResponse = OrderResponse(
            pspReference = "test_psp",
            orderData = "test_order_data",
            amount = null,
            remainingAmount = null,
        )
        delegate.resolveOrderResponse(orderResponse)
        delegate.componentStateFlow.test {
            val state = expectMostRecentItem()

            val expectedOrderRequest = OrderRequest(
                orderData = "test_order_data",
                pspReference = "test_psp",
            )
            assertEquals(expectedOrderRequest, state.data.order)
            assertEquals(GiftCardAction.SendPayment, state.giftCardAction)
            verify(submitHandler).onSubmit(state)
        }
    }

    @Nested
    inner class AnalyticsTest {

        @Test
        fun `when delegate is initialized then analytics manager is initialized`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            analyticsManager.assertIsInitialized()
        }

        @Test
        fun `when delegate is initialized, then render event is tracked`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val expectedEvent = GenericEvents.rendered(TEST_PAYMENT_METHOD_TYPE)
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when onSubmit is called, then submit event is tracked`() {
            delegate.onSubmit()

            val expectedEvent = GenericEvents.submit(TEST_PAYMENT_METHOD_TYPE)
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when component state is valid then PaymentMethodDetails should contain checkoutAttemptId`() = runTest {
            analyticsManager.setCheckoutAttemptId(TEST_CHECKOUT_ATTEMPT_ID)

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateInputData {
                    cardNumber = "5555444433330000"
                    pin = "737"
                }

                assertEquals(TEST_CHECKOUT_ATTEMPT_ID, expectMostRecentItem().data.paymentMethod?.checkoutAttemptId)
            }
        }

        @Test
        fun `when delegate is cleared then analytics manager is cleared`() {
            delegate.onCleared()

            analyticsManager.assertIsCleared()
        }
    }

    @Test
    fun `when pin is not required, then does not matter for validation`() = runTest {
        delegate = createGiftCardDelegate(
            configuration = createCheckoutConfiguration {
                setPinRequired(false)
            },
        )
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        val componentStateFlow = delegate.componentStateFlow.test(testScheduler)

        delegate.updateInputData {
            // Valid card number
            cardNumber = "5555444433330000"
            // Invalid pin
            pin = ""
        }

        assertTrue(componentStateFlow.latestValue.isInputValid)
    }

    private fun createGiftCardDelegate(
        configuration: CheckoutConfiguration = createCheckoutConfiguration(),
        order: OrderRequest? = TEST_ORDER
    ) = DefaultGiftCardDelegate(
        observerRepository = PaymentObserverRepository(),
        paymentMethod = PaymentMethod(type = TEST_PAYMENT_METHOD_TYPE),
        order = order,
        publicKeyRepository = publicKeyRepository,
        componentParams = GiftCardComponentParamsMapper(CommonComponentParamsMapper())
            .mapToParams(configuration, Locale.US, null, null),
        cardEncryptor = cardEncryptor,
        analyticsManager = analyticsManager,
        submitHandler = submitHandler,
        validator = DefaultGiftCardValidator(),
        componentViewType = GiftCardComponentViewType(),
    )

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: GiftCardConfiguration.Builder.() -> Unit = {},
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        amount = amount,
    ) {
        giftCard(configuration)
    }

    private fun giftCardOutputDataWith(
        number: String,
        @Suppress("SameParameterValue") pin: String,
    ) = GiftCardOutputData(
        numberFieldState = GiftCardNumberUtils.validateInputField(number),
        pinFieldState = GiftCardPinUtils.validateInputField(pin),
        // expiry date is valid since it doesn't apply to gift cards
        expiryDateFieldState = FieldState(TEST_EXPIRY_DATE, Validation.Valid),
    )

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
        private const val TEST_CHECKOUT_ATTEMPT_ID = "TEST_CHECKOUT_ATTEMPT_ID"
        private const val TEST_PAYMENT_METHOD_TYPE = "TEST_PAYMENT_METHOD_TYPE"
        private val TEST_EXPIRY_DATE = ExpiryDate(3, 2030)

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
