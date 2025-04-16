/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/3/2025.
 */

package core.adyen.checkout.core.internal.ui.state

import com.adyen.checkout.core.internal.ui.state.model.DelegateFieldState
import com.adyen.checkout.core.internal.ui.state.updater.StateUpdaterRegistry

internal class TestStateUpdaterRegistry : StateUpdaterRegistry<TestDelegateState, TestFieldId> {

    private val fieldStates = mutableMapOf<TestFieldId, DelegateFieldState<*>>()

    fun <T> initialize(defaultFieldState: DelegateFieldState<T>) {
        TestFieldId.entries.map { fieldId ->
            fieldStates[fieldId] = defaultFieldState
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getFieldState(state: TestDelegateState, key: TestFieldId): DelegateFieldState<T> {
        return fieldStates[key] as DelegateFieldState<T>
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> updateFieldState(
        state: TestDelegateState,
        key: TestFieldId,
        fieldState: DelegateFieldState<T>,
    ): TestDelegateState {
        fieldStates[key] = fieldState as DelegateFieldState<Any>
        return state
    }
}
