/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/4/2025.
 */

package com.adyen.checkout.core.internal.ui.state.model

import androidx.annotation.RestrictTo
import androidx.annotation.StringRes

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class ViewFieldState<T>(
    val value: T,
    val hasFocus: Boolean = false,
    @StringRes val errorMessageId: Int? = null,
)
