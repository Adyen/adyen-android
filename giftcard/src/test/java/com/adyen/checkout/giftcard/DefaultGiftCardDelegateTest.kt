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
import com.adyen.checkout.components.repository.PublicKeyRepository
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.cse.TestCardEncrypter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
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
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.io.IOException
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class DefaultGiftCardDelegateTest(
    @Mock private val publicKeyRepository: PublicKeyRepository
) {

    private val cardEncrypter = TestCardEncrypter()

    private val delegate = DefaultGiftCardDelegate(
        paymentMethod = PaymentMethod(),
        publicKeyRepository = publicKeyRepository,
        configuration = GiftCardConfiguration.Builder(
            Locale.US,
            Environment.TEST,
            TEST_CLIENT_KEY
        ).build(),
        cardEncrypter = cardEncrypter,
    )

    @BeforeEach
    fun before() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        cardEncrypter.reset()
    }

    @AfterEach
    fun after() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when fetching the public key fails, then an error is propagated`() = runTest {
        val exception = IOException("Test")
        whenever(publicKeyRepository.fetchPublicKey(any(), any())) doReturn Result.failure(exception)

        delegate.exceptionFlow.test {
            delegate.fetchPublicKey()

            assertEquals(exception, awaitItem().cause)

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
            stubPublicKeyRepository()

            delegate.componentStateFlow.test {
                delegate.createComponentState(GiftCardOutputData("123", "737"))

                skipItems(1)

                assertFalse(awaitItem()!!.isInputValid)
            }
        }

        @Test
        fun `encryption fails, then component state should be invalid`() = runTest {
            stubPublicKeyRepository()
            cardEncrypter.shouldThrowException = true

            delegate.componentStateFlow.test {
                delegate.createComponentState(GiftCardOutputData("5555444433330000", "737"))

                skipItems(1)

                assertFalse(awaitItem()!!.isInputValid)
            }
        }

        @Test
        fun `everything is valid, then component state should be good`() = runTest {
            stubPublicKeyRepository()

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

    private suspend fun stubPublicKeyRepository() {
        whenever(publicKeyRepository.fetchPublicKey(any(), any())) doReturn Result.success(TEST_PUBLIC_KEY)
        delegate.fetchPublicKey()
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_PUBLIC_KEY =
            "10001|1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                "1111111111111111111111111111111111"
    }
}
