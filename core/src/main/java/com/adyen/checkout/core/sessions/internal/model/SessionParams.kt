/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/5/2025.
 */

package com.adyen.checkout.core.sessions.internal.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.data.model.Amount
import java.util.Locale

/**
 * Object that holds values set during sessions setup call.
 * [SessionParams] values by default should have higher priority than values set in client side configurations.
 * Otherwise it can cause server error, since specific configuration is not enabled, but it is being used.
 *
 * Only for some specific cases, if they do not cause server error, client side configurations can have higher priority
 * than [SessionParams] values.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class SessionParams(
    val environment: Environment,
    val clientKey: String,
    val enableStoreDetails: Boolean?,
    val installmentConfiguration: SessionInstallmentConfiguration?,
    val showRemovePaymentMethodButton: Boolean?,
    val amount: Amount?,
    val returnUrl: String?,
    val shopperLocale: Locale?,
)
