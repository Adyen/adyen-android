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

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface FieldChangeListener<FI : FieldId> {
    fun <T> onFieldValueChanged(fieldId: FI, value: T)

    fun onFieldFocusChanged(fieldId: FI, hasFocus: Boolean)
}
