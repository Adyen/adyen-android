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
import com.adyen.checkout.components.repository.TestPublicKeyRepository
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.cse.TestCardEncrypter
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
            delegate.fetchPublicKey()

            assertEquals(publicKeyRepository.errorResult.exceptionOrNull(), awaitItem().cause)

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

                skipItems(1)

                assertFalse(awaitItem()!!.isReady)
            }
        }

        @Test
        fun `output data is invalid, then component state should be invalid`() = runTest {
            delegate.fetchPublicKey()
            delegate.componentStateFlow.test {
                delegate.createComponentState(GiftCardOutputData("123", "737"))

                skipItems(1)

                assertFalse(awaitItem()!!.isInputValid)
            }
        }

        @Test
        fun `encryption fails, then component state should be invalid`() = runTest {
            delegate.fetchPublicKey()
            cardEncrypter.shouldThrowException = true

            delegate.componentStateFlow.test {
                delegate.createComponentState(GiftCardOutputData("5555444433330000", "737"))

                skipItems(1)
                val item = awaitItem()

                assertFalse(item!!.isInputValid)
            }
        }

        @Test
        fun `everything is valid, then component state should be good`() = runTest {
            delegate.fetchPublicKey()
            delegate.componentStateFlow.test {
                delegate.createComponentState(GiftCardOutputData("5555444433330000", "737"))

                skipItems(1)

                val componentState = requireNotNull(awaitItem())

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
