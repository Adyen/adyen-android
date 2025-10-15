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
) : StateManager<V, C> {

    private val _viewState = MutableStateFlow(viewStateFactory.createDefaultViewState())
    override val viewState: StateFlow<V> = _viewState.asStateFlow()

    private val _componentState = MutableStateFlow(componentStateFactory.createDefaultComponentState())
    override val componentState: StateFlow<C> = _componentState.asStateFlow()

    override fun updateViewState(update: V.() -> V) {
        _viewState.update { it.update() }
    }

    override fun updateComponentState(update: C.() -> C) {
        _componentState.update { it.update() }
    }
}
