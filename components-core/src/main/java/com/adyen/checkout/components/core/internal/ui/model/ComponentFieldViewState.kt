/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/1/2025.
 */

package com.adyen.checkout.components.core.internal.ui.model

import androidx.annotation.RestrictTo
import androidx.annotation.StringRes

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class ComponentFieldViewState<T>(
    val value: T,
    val hasFocus: Boolean = false,
    @StringRes val errorMessageId: Int? = null,
)
