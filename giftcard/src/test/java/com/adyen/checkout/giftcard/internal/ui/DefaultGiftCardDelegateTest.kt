/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/7/2022.
 */

package com.adyen.checkout.giftcard.internal.ui

import app.cash.turbine.test
import com.adyen.checkout.components.analytics.AnalyticsRepository
import com.adyen.checkout.components.base.ButtonComponentParamsMapper
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.test.TestPublicKeyRepository
import com.adyen.checkout.components.ui.SubmitHandler
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.cse.test.TestCardEncrypter
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.giftcard.GiftCardConfiguration
import com.adyen.checkout.giftcard.internal.ui.model.GiftCardOutputData
import com.adyen.checkout.test.TestDispatcherExtension
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
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class DefaultGiftCardDelegateTest(
    @Mock private val analyticsRepository: AnalyticsRepository,
    @Mock private val submitHandler: SubmitHandler<GiftCardComponentState>,
) {

    private lateinit var cardEncrypter: TestCardEncrypter
    private lateinit var publicKeyRepository: TestPublicKeyRepository
    private lateinit var delegate: DefaultGiftCardDelegate

    @BeforeEach
    fun before() {
        cardEncrypter = TestCardEncrypter()
        publicKeyRepository = TestPublicKeyRepository()
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
                delegate.updateComponentState(GiftCardOutputData("5555444433330000", "737"))

                val componentState = expectMostRecentItem()

                assertFalse(componentState.isReady)
                assertEquals(null, componentState.lastFourDigits)
            }
        }

        @Test
        fun `output data is invalid, then component state should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateComponentState(GiftCardOutputData("123", "737"))

                val componentState = expectMostRecentItem()

                assertTrue(componentState.isReady)
                assertFalse(componentState.isInputValid)
                assertEquals(null, componentState.lastFourDigits)
            }
        }

        @Test
        fun `encryption fails, then component state should be invalid`() = runTest {
            cardEncrypter.shouldThrowException = true

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateComponentState(GiftCardOutputData("5555444433330000", "737"))

                val componentState = expectMostRecentItem()

                assertTrue(componentState.isReady)
                assertFalse(componentState.isInputValid)
                assertEquals(null, componentState.lastFourDigits)
            }
        }

        @Test
        fun `everything is valid, then component state should be good`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateComponentState(GiftCardOutputData("5555444433330000", "737"))

                val componentState = expectMostRecentItem()

                assertNotNull(componentState.data.paymentMethod)
                assertTrue(componentState.isInputValid)
                assertTrue(componentState.isReady)
                assertEquals("0000", componentState.lastFourDigits)
                assertEquals(TEST_ORDER, componentState.data.order)
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
            delegate = createGiftCardDelegate(
                configuration = getDefaultGiftCardConfigurationBuilder()
                    .setSubmitButtonVisible(false)
                    .build()
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `when submit button is configured to be visible, then it should show`() {
            delegate = createGiftCardDelegate(
                configuration = getDefaultGiftCardConfigurationBuilder()
                    .setSubmitButtonVisible(true)
                    .build()
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

    private fun createGiftCardDelegate(
        configuration: GiftCardConfiguration = getDefaultGiftCardConfigurationBuilder().build()
    ) = DefaultGiftCardDelegate(
        observerRepository = PaymentObserverRepository(),
        paymentMethod = PaymentMethod(),
        order = TEST_ORDER,
        publicKeyRepository = publicKeyRepository,
        componentParams = ButtonComponentParamsMapper().mapToParams(configuration),
        cardEncrypter = cardEncrypter,
        analyticsRepository = analyticsRepository,
        submitHandler = submitHandler,
    )

    private fun getDefaultGiftCardConfigurationBuilder() = GiftCardConfiguration.Builder(
        Locale.US,
        Environment.TEST,
        TEST_CLIENT_KEY
    )

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
    }
}
