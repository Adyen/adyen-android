/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 12/3/2024.
 */

package com.adyen.checkout.upi.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.Environment

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class UPIApp(
    val id: String,
    val name: String,
    val environment: Environment,
)
