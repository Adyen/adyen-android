/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/4/2025.
 */

package com.adyen.checkout.core.components.internal.ui.state

import androidx.annotation.RestrictTo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultViewStateManager<V : ViewState>(
    factory: ViewStateFactory<V>,
    private val validator: ViewStateValidator<V>,
) : ViewStateManager<V> {

    private val _state = MutableStateFlow(factory.createDefaultViewState())
    override val state: StateFlow<V> = _state.asStateFlow()

    override val isValid: Boolean
        get() = validator.isValid(_state.value)

    override fun update(update: V.() -> V) {
        val newState = _state.value.update()
        val validatedState = validator.validate(newState)
        _state.value = validatedState
    }

    override fun highlightAllFieldValidationErrors() {
        _state.value = validator.highlightAllValidationErrors(_state.value)
    }
}
