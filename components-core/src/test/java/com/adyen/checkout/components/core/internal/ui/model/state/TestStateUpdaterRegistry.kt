/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/3/2025.
 */

package com.adyen.checkout.components.core.internal.ui.model.state

import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldDelegateState

internal class TestStateUpdaterRegistry : StateUpdaterRegistry<TestDelegateState, TestFieldId> {

    private val fieldStates = mutableMapOf<TestFieldId, ComponentFieldDelegateState<*>>()

    fun <T> initialize(defaultFieldState: ComponentFieldDelegateState<T>) {
        TestFieldId.entries.map { fieldId ->
            fieldStates[fieldId] = defaultFieldState
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getFieldState(state: TestDelegateState, key: TestFieldId): ComponentFieldDelegateState<T> {
        return fieldStates[key] as ComponentFieldDelegateState<T>
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> updateFieldState(
        state: TestDelegateState,
        key: TestFieldId,
        fieldState: ComponentFieldDelegateState<T>,
    ): TestDelegateState {
        fieldStates[key] = fieldState as ComponentFieldDelegateState<Any>
        return state
    }
}
