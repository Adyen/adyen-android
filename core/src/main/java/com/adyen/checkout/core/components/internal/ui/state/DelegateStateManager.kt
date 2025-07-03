/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/4/2025.
 */

package com.adyen.checkout.core.components.internal.ui.state

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.internal.ui.state.model.FieldId
import kotlinx.coroutines.flow.StateFlow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface DelegateStateManager<S : DelegateState, FI : FieldId> {

    val state: StateFlow<S>

    val isValid: Boolean

    fun updateState(update: S.() -> S)

    fun <T> updateFieldValue(
        fieldId: FI,
        value: T? = null,
    )

    fun updateFieldFocus(
        fieldId: FI,
        hasFocus: Boolean,
    )

    fun highlightAllFieldValidationErrors()
}
