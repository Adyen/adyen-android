/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 23/6/2026.
 */

package com.adyen.checkout.googlepay.internal.helper

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal suspend fun <T> Task<T>.awaitTask(): Task<T> {
    return if (isComplete) {
        this
    } else {
        suspendCancellableCoroutine { cont ->
            addOnCompleteListener { completedTask -> cont.resume(completedTask) }
        }
    }
}
