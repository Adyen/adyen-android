/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/5/2025.
 */

package com.adyen.checkout.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.data.model.Amount
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ComponentParams {
    val shopperLocale: Locale
    val environment: Environment
    val clientKey: String
    val analyticsParams: AnalyticsParams
    val isCreatedByDropIn: Boolean
    val amount: Amount?
}
