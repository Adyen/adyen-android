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
interface StateUpdaterRegistry<K, S> {
    fun <T> getFieldState(key: K, state: S): ComponentFieldDelegateState<T>

    fun <T> updateFieldState(key: K, state: S, fieldState: ComponentFieldDelegateState<T>): S
}
