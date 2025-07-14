/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/7/2025.
 */

package com.adyen.checkout.core.action.internal

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.components.CheckoutConfiguration
import kotlinx.coroutines.CoroutineScope

internal object ActionProvider {

    private val factories = mutableMapOf<String, ActionFactory<*>>()

    fun register(
        action: Action,
        factory: ActionFactory<*>,
    ) {
        val actionType = action.type
        require(!actionType.isNullOrEmpty()) {
            "The action type cannot be empty or null"
        }

        factories[actionType] = factory
    }

    /**
     * Create an [ActionDelegate] using an [ActionFactory].
     *
     * @param action The action to be handled.
     * @param coroutineScope The [CoroutineScope] to be used by the delegate.
     * @param checkoutConfiguration The global checkout configuration.
     * @param savedStateHandle The [SavedStateHandle] to be used by the delegate.
     *
     * @return [ActionDelegate] for given action type.
     * @throws IllegalStateException If a factory for the provided action type is not registered.
     */
    fun get(
        action: Action,
        coroutineScope: CoroutineScope,
        checkoutConfiguration: CheckoutConfiguration,
        savedStateHandle: SavedStateHandle,
    ): ActionDelegate {
        return factories[action.type]?.create(
            coroutineScope = coroutineScope,
            checkoutConfiguration = checkoutConfiguration,
            savedStateHandle = savedStateHandle,
        ) ?: run {
            error("Factory for action type: ${action.type} is not registered.")
        }
    }
}
