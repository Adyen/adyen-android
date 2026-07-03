/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/6/2026.
 */

package com.adyen.checkout.authentication.internal.ui

import com.adyen.threeds2.ChallengeResult
import com.adyen.threeds2.ChallengeStatusHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

internal class SharedChallengeStatusHandlerTest {

    @BeforeEach
    fun setup() {
        SharedChallengeStatusHandler.reset()
    }

    @Nested
    @DisplayName("when onCompletion is called and")
    inner class OnCompletionTest {

        @Test
        fun `listener is set, then result is forwarded to listener`() {
            // GIVEN
            val listener = TestChallengeStatusHandler()
            SharedChallengeStatusHandler.onCompletionListener = listener
            val result = ChallengeResult.Completed(transactionStatus = "Y")

            // WHEN
            SharedChallengeStatusHandler.onCompletion(result)

            // THEN
            assertEquals(result, listener.lastResult)
            assertEquals(1, listener.timesOnCompletionCalled)
        }

        @Test
        fun `listener is not set, then result is queued`() {
            // GIVEN
            val result = ChallengeResult.Completed(transactionStatus = "Y")

            // WHEN
            SharedChallengeStatusHandler.onCompletion(result)

            // THEN - result is queued, verified by setting a listener afterwards
            val listener = TestChallengeStatusHandler()
            SharedChallengeStatusHandler.onCompletionListener = listener
            assertEquals(result, listener.lastResult)
            assertEquals(1, listener.timesOnCompletionCalled)
        }

        @Test
        fun `listener is not set and multiple results arrive, then only last result is queued`() {
            // GIVEN
            val firstResult = ChallengeResult.Completed(transactionStatus = "Y")
            val secondResult = ChallengeResult.Cancelled(
                transactionStatus = "C",
                additionalDetails = "details",
            )

            // WHEN
            SharedChallengeStatusHandler.onCompletion(firstResult)
            SharedChallengeStatusHandler.onCompletion(secondResult)

            // THEN
            val listener = TestChallengeStatusHandler()
            SharedChallengeStatusHandler.onCompletionListener = listener
            assertEquals(secondResult, listener.lastResult)
            assertEquals(1, listener.timesOnCompletionCalled)
        }
    }

    @Nested
    @DisplayName("when setting listener and")
    inner class SetListenerTest {

        @Test
        fun `result was queued, then queued result is delivered immediately`() {
            // GIVEN
            val result = ChallengeResult.Timeout(
                transactionStatus = "T",
                additionalDetails = "timeout",
            )
            SharedChallengeStatusHandler.onCompletion(result)

            // WHEN
            val listener = TestChallengeStatusHandler()
            SharedChallengeStatusHandler.onCompletionListener = listener

            // THEN
            assertEquals(result, listener.lastResult)
            assertEquals(1, listener.timesOnCompletionCalled)
        }

        @Test
        fun `no result was queued, then listener is not called`() {
            // GIVEN
            val listener: ChallengeStatusHandler = mock()

            // WHEN
            SharedChallengeStatusHandler.onCompletionListener = listener

            // THEN
            verify(listener, never()).onCompletion(any())
        }
    }

    @Nested
    @DisplayName("when reset is called")
    inner class ResetTest {

        @Test
        fun `then queued result is cleared`() {
            // GIVEN
            val result = ChallengeResult.Completed(transactionStatus = "Y")
            SharedChallengeStatusHandler.onCompletion(result)

            // WHEN
            SharedChallengeStatusHandler.reset()

            // THEN - setting a new listener should not receive the old result
            val listener = TestChallengeStatusHandler()
            SharedChallengeStatusHandler.onCompletionListener = listener
            assertEquals(0, listener.timesOnCompletionCalled)
        }

        @Test
        fun `then listener is cleared`() {
            // GIVEN
            val listener = TestChallengeStatusHandler()
            SharedChallengeStatusHandler.onCompletionListener = listener

            // WHEN
            SharedChallengeStatusHandler.reset()

            // THEN - sending a result should queue it, not deliver it to old listener
            val result = ChallengeResult.Completed(transactionStatus = "Y")
            SharedChallengeStatusHandler.onCompletion(result)
            assertEquals(0, listener.timesOnCompletionCalled)
        }
    }
}

private class TestChallengeStatusHandler : ChallengeStatusHandler {

    var lastResult: ChallengeResult? = null
    var timesOnCompletionCalled = 0

    override fun onCompletion(result: ChallengeResult) {
        lastResult = result
        timesOnCompletionCalled++
    }
}
