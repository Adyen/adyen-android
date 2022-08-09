/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/7/2022.
 */

package com.adyen.checkout.giftcard

import app.cash.turbine.test
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.test.TestPublicKeyRepository
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.cse.test.TestCardEncrypter
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
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class DefaultGiftCardDelegateTest {

    private lateinit var cardEncrypter: TestCardEncrypter
    private lateinit var publicKeyRepository: TestPublicKeyRepository
    private lateinit var delegate: DefaultGiftCardDelegate

    @BeforeEach
    fun before() {
        cardEncrypter = TestCardEncrypter()
        publicKeyRepository = TestPublicKeyRepository()

        delegate = DefaultGiftCardDelegate(
            paymentMethod = PaymentMethod(),
            publicKeyRepository = publicKeyRepository,
            configuration = GiftCardConfiguration.Builder(
                Locale.US,
                Environment.TEST,
                TEST_CLIENT_KEY
            ).build(),
            cardEncrypter = cardEncrypter,
        )
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
                delegate.createComponentState(GiftCardOutputData("5555444433330000", "737"))

                val componentState = requireNotNull(expectMostRecentItem())

                assertFalse(componentState.isReady)
                assertEquals(null, componentState.lastFourDigits)
            }
        }

        @Test
        fun `output data is invalid, then component state should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.createComponentState(GiftCardOutputData("123", "737"))

                val componentState = requireNotNull(expectMostRecentItem())

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
                delegate.createComponentState(GiftCardOutputData("5555444433330000", "737"))

                val componentState = requireNotNull(expectMostRecentItem())

                assertTrue(componentState.isReady)
                assertFalse(componentState.isInputValid)
                assertEquals(null, componentState.lastFourDigits)
            }
        }

        @Test
        fun `everything is valid, then component state should be good`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.createComponentState(GiftCardOutputData("5555444433330000", "737"))

                val componentState = requireNotNull(expectMostRecentItem())

                assertNotNull(componentState.data.paymentMethod)
                assertTrue(componentState.isInputValid)
                assertTrue(componentState.isReady)
                assertEquals("0000", componentState.lastFourDigits)
            }
        }
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
