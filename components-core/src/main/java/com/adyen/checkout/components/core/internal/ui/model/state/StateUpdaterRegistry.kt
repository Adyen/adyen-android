/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 4/2/2025.
 */

package com.adyen.checkout.components.core.internal.ui.model.state

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldDelegateState

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface StateUpdaterRegistry<S, K> {
    fun <T> getFieldState(state: S, key: K): ComponentFieldDelegateState<T>

    fun <T> updateFieldState(state: S, key: K, fieldState: ComponentFieldDelegateState<T>): S
}
