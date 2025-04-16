/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/4/2025.
 */

package com.adyen.checkout.core.internal.ui.state.updater

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.internal.ui.state.DelegateState
import com.adyen.checkout.core.internal.ui.state.model.DelegateFieldState
import com.adyen.checkout.core.internal.ui.state.model.FieldId

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface StateUpdaterRegistry<S : DelegateState, FI : FieldId> {
    fun <T> getFieldState(state: S, fieldId: FI): DelegateFieldState<T>

    fun <T> updateFieldState(state: S, fieldId: FI, fieldState: DelegateFieldState<T>): S
}
