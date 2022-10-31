/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/10/2022.
 */

package com.adyen.checkout.components.flow

import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <T> Flow<T>.mapToCallbackWithLifeCycle(
    lifecycleOwner: LifecycleOwner,
    coroutineScope: CoroutineScope,
    callback: (T) -> Unit
) {
    flowWithLifecycle(lifecycleOwner.lifecycle)
        .onEach { callback(it) }
        .launchIn(coroutineScope)
}
