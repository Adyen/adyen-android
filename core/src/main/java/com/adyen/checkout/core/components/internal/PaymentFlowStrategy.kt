/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 31/12/2025.
 */

package com.adyen.checkout.core.components.internal

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry
import kotlinx.coroutines.CoroutineScope

internal interface PaymentFlowStrategy {

    val navigationStartingPoint: NavKey

    val navigationEntries: Map<NavKey, CheckoutNavEntry>

    fun observe(lifecycle: Lifecycle, coroutineScope: CoroutineScope, onResult: (CheckoutResult) -> Unit)

    fun submit()

    fun handleAction(
        action: Action,
        lifecycle: Lifecycle,
        coroutineScope: CoroutineScope,
        onActionComponentCreated: (NavKey) -> Unit,
        onResult: (CheckoutResult) -> Unit,
    )

    fun handleIntent(intent: Intent, coroutineScope: CoroutineScope)

    fun onCleared()
}
