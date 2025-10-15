/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/4/2025.
 */

package com.adyen.checkout.core.components.internal.ui.state

import androidx.annotation.RestrictTo
import kotlinx.coroutines.flow.StateFlow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface StateManager<V : ViewState, C : ComponentState> {

    val viewState: StateFlow<V>

    val componentState: StateFlow<C>

    fun updateViewState(update: V.() -> V)

    fun updateComponentState(update: C.() -> C)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ViewState

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ComponentState
