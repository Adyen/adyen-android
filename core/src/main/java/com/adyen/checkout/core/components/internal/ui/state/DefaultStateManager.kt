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
import kotlinx.coroutines.flow.update

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultStateManager<V : ViewState, C : ComponentState>(
    viewStateFactory: ViewStateFactory<V>,
    componentStateFactory: ComponentStateFactory<C>,
    private val validator: ViewStateValidator<V, C>,
) : StateManager<V, C> {

    private var _componentState: C = componentStateFactory.createInitialState()
    override val componentState: C
        get() = _componentState

    override val isValid: Boolean
        get() = validator.isValid(viewState.value)

    private val _viewState = MutableStateFlow(viewStateFactory.createDefaultViewState())
    override val viewState: StateFlow<V> = _viewState.asStateFlow()

    override fun updateViewState(update: V.() -> V) {
        _viewState.update { it.update() }
    }

    override fun updateViewStateAndValidate(update: V.() -> V) {
        val newState = viewState.value.update()
        val validatedState = validator.validate(newState, componentState)
        _viewState.update { validatedState }
    }

    override fun updateComponentState(update: C.() -> C) {
        _componentState = _componentState.update()
        val validatedState = validator.validate(viewState.value, componentState)
        _viewState.update { validatedState }
    }

    override fun highlightAllValidationErrors() {
        val validatedState = validator.highlightAllValidationErrors(viewState.value)
        _viewState.update { validatedState }
    }
}
