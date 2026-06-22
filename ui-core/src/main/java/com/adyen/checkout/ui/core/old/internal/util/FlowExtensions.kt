/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.util

import android.view.View
import androidx.annotation.RestrictTo
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import com.adyen.checkout.components.core.internal.util.mergeStateFlows
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun mergeViewFlows(
    scope: CoroutineScope,
    paymentMethodViewFlow: Flow<ComponentViewType?>,
    genericActionViewFlow: Flow<ComponentViewType?>,
    initialValue: ComponentViewType? = null,
    // Lazily is the default, because WhileSubscribed re-emits initial values when f.e. rotating the phone.
    started: SharingStarted = SharingStarted.Lazily,
): StateFlow<ComponentViewType?> = mergeStateFlows(
    scope = scope,
    initialValue = initialValue,
    started = started,
    // genericActionViewFlow's initial value is null. We don't need this value as it breaks the desired behaviour.
    flows = arrayOf(paymentMethodViewFlow, genericActionViewFlow.drop(1)),
)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <T> Flow<T>.collectWithLifecycle(
    view: View,
    coroutineScope: CoroutineScope,
    action: (T) -> Unit
): Job {
    val lifecycleOwner = view.findViewTreeLifecycleOwner()
    return if (lifecycleOwner != null) {
        this.flowWithLifecycle(lifecycleOwner.lifecycle)
            .onEach { action(it) }
            .launchIn(coroutineScope)
    } else {
        this.onEach { action(it) }
            .launchIn(coroutineScope)
    }
}

