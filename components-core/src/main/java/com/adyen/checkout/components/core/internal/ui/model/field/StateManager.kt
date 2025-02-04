/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 4/2/2025.
 */

package com.adyen.checkout.components.core.internal.ui.model.field

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface StateManager<S, FI> {
    fun <T> updateField(
        state: S,
        fieldId: FI,
        value: T?,
        hasFocus: Boolean?,
        shouldHighlightValidationError: Boolean?,
    ): S
}
