/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 30/1/2025.
 */

package com.adyen.checkout.ui.core.internal.ui

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface UIEventDelegate<FI> {
    fun <T> onFieldValueChanged(fieldId: FI, value: T)

    fun onFieldFocusChanged(fieldId: FI, hasFocus: Boolean)
}
