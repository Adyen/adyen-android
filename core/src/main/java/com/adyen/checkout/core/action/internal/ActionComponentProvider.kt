/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/7/2025.
 */

package com.adyen.checkout.core.action.internal

import androidx.annotation.RestrictTo
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.components.CheckoutConfiguration
import kotlinx.coroutines.CoroutineScope

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object ActionComponentProvider {

    private val factories = mutableMapOf<String, ActionFactory<*>>()

    fun register(
        actionType: String,
        factory: ActionFactory<*>,
    ) {
        factories[actionType] = factory
    }

    /**
     * Create an [ActionComponent] using an [ActionFactory].
     *
     * @param action The action to be handled.
     * @param coroutineScope The [CoroutineScope] to be used by the component.
     * @param checkoutConfiguration The global checkout configuration.
     * @param savedStateHandle The [SavedStateHandle] to be used by the component.
     *
     * @return [ActionComponent] for given action type.
     * @throws IllegalStateException If a factory for the provided action type is not registered.
     */
    fun get(
        action: Action,
        coroutineScope: CoroutineScope,
        checkoutConfiguration: CheckoutConfiguration,
        savedStateHandle: SavedStateHandle,
    ): ActionComponent {
        return factories[action.type]?.create(
            coroutineScope = coroutineScope,
            checkoutConfiguration = checkoutConfiguration,
            savedStateHandle = savedStateHandle,
        ) ?: run {
            error("Factory for action type: ${action.type} is not registered.")
        }
    }
}
