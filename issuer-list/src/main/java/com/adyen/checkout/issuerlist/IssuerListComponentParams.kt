/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2022.
 */

package com.adyen.checkout.issuerlist

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.api.Environment
import kotlinx.parcelize.Parcelize
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
data class IssuerListComponentParams(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val isAnalyticsEnabled: Boolean,
    override val isCreatedByDropIn: Boolean,
    override val amount: Amount,
    val viewType: IssuerListViewType,
    val hideIssuerLogos: Boolean,
) : ComponentParams
