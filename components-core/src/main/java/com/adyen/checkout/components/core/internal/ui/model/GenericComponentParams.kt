/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2022.
 */

package com.adyen.checkout.components.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.core.Environment
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class GenericComponentParams(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsParams: AnalyticsParams,
    override val isCreatedByDropIn: Boolean,
    override val amount: Amount,
) : ComponentParams
