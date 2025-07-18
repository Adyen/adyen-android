/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/3/2025.
 */

package com.adyen.checkout.core.common.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.model.ComponentFieldState
import com.adyen.checkout.core.components.internal.ui.state.updater.StateUpdaterRegistry

internal class TestStateUpdaterRegistry : StateUpdaterRegistry<TestComponentState, TestFieldId> {

    private val fieldStates = mutableMapOf<TestFieldId, ComponentFieldState<*>>()

    fun <T> initialize(defaultFieldState: ComponentFieldState<T>) {
        TestFieldId.entries.map { fieldId ->
            fieldStates[fieldId] = defaultFieldState
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getFieldState(state: TestComponentState, key: TestFieldId): ComponentFieldState<T> {
        return fieldStates[key] as ComponentFieldState<T>
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> updateFieldState(
        state: TestComponentState,
        key: TestFieldId,
        fieldState: ComponentFieldState<T>,
    ): TestComponentState {
        fieldStates[key] = fieldState as ComponentFieldState<Any>
        return state
    }
}
