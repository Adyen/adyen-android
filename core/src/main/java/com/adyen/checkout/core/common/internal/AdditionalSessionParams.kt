/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/5/2026.
 */

package com.adyen.checkout.core.common.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.sessions.internal.model.SessionInstallmentConfiguration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class AdditionalSessionParams(
    val enableStoreDetails: Boolean?,
    val installmentConfiguration: SessionInstallmentConfiguration?,
    val showRemovePaymentMethodButton: Boolean?,
    val returnUrl: String?,
)
