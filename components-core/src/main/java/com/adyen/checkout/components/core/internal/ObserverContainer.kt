/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/11/2022.
 */

package com.adyen.checkout.components.core.internal

import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.internal.util.mapToCallbackWithLifeCycle
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ObserverContainer {

    private var observerJobs: MutableList<Job> = mutableListOf()

    internal fun <T> Flow<T>.observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (T) -> Unit,
    ) {
        mapToCallbackWithLifeCycle(
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback,
        ).also {
            observerJobs.add(it)
        }
    }

    internal fun removeObservers() {
        if (observerJobs.isEmpty()) return
        adyenLog(AdyenLogLevel.DEBUG) { "cleaning up existing observer" }
        observerJobs.forEach { it.cancel() }
        observerJobs.clear()
    }
}
