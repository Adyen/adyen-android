/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/3/2023.
 */

package com.adyen.checkout.core.sessions.internal.model

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class SessionInstallmentOptionsParams(
    val plans: List<String>?,
    val preselectedValue: Int?,
    val values: List<Int>?
)
