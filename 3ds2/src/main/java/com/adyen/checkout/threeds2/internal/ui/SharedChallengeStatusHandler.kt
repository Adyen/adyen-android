/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 1/12/2025.
 */

package com.adyen.checkout.threeds2.internal.ui

import com.adyen.threeds2.ChallengeResult
import com.adyen.threeds2.ChallengeStatusHandler

internal object SharedChallengeStatusHandler : ChallengeStatusHandler {

    var onCompletionListener: ChallengeStatusHandler? = null
        set(value) {
            field = value
            queuedResult?.let { onCompletion(it) }
        }

    private var queuedResult: ChallengeResult? = null

    override fun onCompletion(result: ChallengeResult) {
        onCompletionListener
            ?.onCompletion(result)
            ?.also {
                queuedResult = null
            } ?: run {
            queuedResult = result
        }
    }

    fun reset() {
        onCompletionListener = null
        queuedResult = null
    }
}
