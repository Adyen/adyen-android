/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/7/2025.
 */

package com.adyen.checkout.core.action.internal

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.CheckoutParams
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object ActionComponentProvider {

    private val factories = ConcurrentHashMap<String, ActionFactory<*, *>>()

    fun register(
        actionType: String,
        factory: ActionFactory<*, *>,
    ) {
        factories[actionType] = factory
    }

    fun get(
        action: Action,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        params: CheckoutParams,
        savedStateHandle: SavedStateHandle,
    ): ActionComponent {
        @Suppress("UNCHECKED_CAST")
        val factory = factories[action.type] as? ActionFactory<Action, *>
        return factory?.create(
            action = action,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            params = params,
            savedStateHandle = savedStateHandle,
        ) ?: run {
            error("Factory for action type: ${action.type} is not registered.")
        }
    }

    /**
     * Clears all registered factories. Should only be used in tests.
     */
    @VisibleForTesting
    internal fun clear() {
        factories.clear()
    }

    /**
     * Returns the number of registered factories. Should only be used in tests.
     */
    @VisibleForTesting
    internal fun getFactoriesCount(): Int {
        return factories.size
    }
}
