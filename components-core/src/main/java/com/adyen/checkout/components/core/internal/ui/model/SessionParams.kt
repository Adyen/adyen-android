/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/3/2023.
 */

package com.adyen.checkout.components.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.Amount
import java.util.Locale

/**
 * Object that holds values set during sessions setup call.
 * [SessionParams] values should always have higher priority than values set in client side configurations. Otherwise
 * it can cause server error, since specific configuration is not enabled, but it is being used.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class SessionParams(
    val enableStoreDetails: Boolean?,
    val installmentConfiguration: SessionInstallmentConfiguration?,
    val showRemovePaymentMethodButton: Boolean?,
    val amount: Amount?,
    val returnUrl: String?,
    val shopperLocale: Locale?,
)
