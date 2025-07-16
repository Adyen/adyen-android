/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/4/2025.
 */

package com.adyen.checkout.core.components.internal.ui.state.updater

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.internal.ui.state.ComponentState

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface StateUpdater<S : ComponentState, FS> {
    fun getFieldState(state: S): FS

    fun updateFieldState(state: S, fieldState: FS): S
}
