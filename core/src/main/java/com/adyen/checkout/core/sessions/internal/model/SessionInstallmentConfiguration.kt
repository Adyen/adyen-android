/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/11/2023.
 */

package com.adyen.checkout.core.sessions.internal.model

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class SessionInstallmentConfiguration(
    val installmentOptions: Map<String, SessionInstallmentOptionsParams?>?,
    val showInstallmentAmount: Boolean?
)
