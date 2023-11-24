/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/2/2021.
 */

package com.adyen.checkout.components.core.internal.ui.model

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class FieldState<T>(
    val value: T,
    val validation: Validation
)
