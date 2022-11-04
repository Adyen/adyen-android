/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/11/2022.
 */

package com.adyen.checkout.components.repository

import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.flow.mapToCallbackWithLifeCycle
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
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
            callback = callback
        ).also {
            observerJobs.add(it)
        }
    }

    internal fun removeObservers() {
        if (observerJobs.isEmpty()) return
        Logger.d(TAG, "cleaning up existing observer")
        observerJobs.forEach { it.cancel() }
        observerJobs.clear()
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
