/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/5/2025.
 */

package com.adyen.checkout.core.components.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.analytics.internal.AnalyticsParams
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.data.model.Amount
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class CommonComponentParams(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsParams: AnalyticsParams,
    override val isCreatedByDropIn: Boolean,
    override val amount: Amount?,
) : ComponentParams
