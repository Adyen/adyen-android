/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/5/2024.
 */

package com.adyen.checkout.adyen3ds2.internal.ui

import androidx.annotation.VisibleForTesting
import com.adyen.threeds2.ChallengeResult
import com.adyen.threeds2.ChallengeStatusHandler

internal object SharedChallengeStatusHandler : ChallengeStatusHandler {

    var onCompletionListener: ChallengeStatusHandler? = null
        set(value) {
            field = value
            resultQueue?.let { onCompletion(it) }
        }

    private var resultQueue: ChallengeResult? = null

    override fun onCompletion(result: ChallengeResult) {
        onCompletionListener
            ?.onCompletion(result)
            ?.also {
                resultQueue = null
            } ?: run {
            resultQueue = result
        }
    }

    @VisibleForTesting
    internal fun reset() {
        onCompletionListener = null
        resultQueue = null
    }
}
