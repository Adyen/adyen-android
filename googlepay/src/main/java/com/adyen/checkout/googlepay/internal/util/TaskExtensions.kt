/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/5/2024.
 */

package com.adyen.checkout.googlepay.internal.util

import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executor
import kotlin.coroutines.resume

internal suspend fun <T> Task<T>.awaitTask(cancellationTokenSource: CancellationTokenSource? = null): Task<T> {
    return if (isComplete) {
        this
    } else {
        suspendCancellableCoroutine { cont ->
            // Run the callback directly to avoid unnecessarily scheduling on the main thread.
            addOnCompleteListener(DirectExecutor(), cont::resume)

            cancellationTokenSource?.let { cancellationSource ->
                cont.invokeOnCancellation { cancellationSource.cancel() }
            }
        }
    }
}

/**
 * An [Executor] that just directly executes the [Runnable].
 */
private class DirectExecutor : Executor {
    override fun execute(runnable: Runnable) {
        runnable.run()
    }
}
