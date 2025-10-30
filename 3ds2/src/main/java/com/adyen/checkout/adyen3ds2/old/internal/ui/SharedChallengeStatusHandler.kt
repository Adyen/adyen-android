/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/5/2024.
 */

package com.adyen.checkout.adyen3ds2.old.internal.ui

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
