/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/10/2025.
 */

package com.adyen.checkout.adyen3ds2.old.internal.ui

import com.adyen.checkout.threeds2.old.internal.ui.SharedChallengeStatusHandler
import com.adyen.threeds2.ChallengeResult
import com.adyen.threeds2.ChallengeStatusHandler
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SharedChallengeStatusHandlerTest {

    @BeforeEach
    fun beforeEach() {
        SharedChallengeStatusHandler.reset()
    }

    @Test
    fun `when onCompletion is triggered, then listener is called`() {
        val onCompletionListener = TestOnCompletionListener()
        SharedChallengeStatusHandler.onCompletionListener = onCompletionListener

        SharedChallengeStatusHandler.onCompletion(ChallengeResult.Completed("test"))

        onCompletionListener.assertOnCompletionCalled()
    }

    @Test
    fun `when onCompletion is triggered and no listener is set, then onCompletion is queued until a listener is set`() {
        val onCompletionListener = TestOnCompletionListener()
        SharedChallengeStatusHandler.onCompletion(ChallengeResult.Completed("test"))

        SharedChallengeStatusHandler.onCompletionListener = onCompletionListener

        onCompletionListener.assertOnCompletionCalled()
    }

    private class TestOnCompletionListener : ChallengeStatusHandler {

        private var timesOnCompletionCalled = 0

        override fun onCompletion(result: ChallengeResult) {
            timesOnCompletionCalled++
        }

        fun assertOnCompletionCalled() {
            assert(timesOnCompletionCalled > 0)
        }
    }
}
