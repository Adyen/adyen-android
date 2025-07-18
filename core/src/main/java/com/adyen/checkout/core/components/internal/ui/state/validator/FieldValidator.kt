/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/1/2025.
 */

package com.adyen.checkout.core.components.internal.ui.state.validator

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.internal.ui.state.ComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.Validation

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun interface FieldValidator<S : ComponentState, T> {
    fun validate(state: S, input: T): Validation
}
