/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2022.
 */

package com.adyen.checkout.bacs

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.api.Environment
import java.util.Locale
import kotlinx.parcelize.Parcelize

@Parcelize
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
// TODO check if this class is still needed once all params support [Amount]
data class BacsDirectDebitComponentParams(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val isAnalyticsEnabled: Boolean,
    override val isCreatedByDropIn: Boolean,
    val amount: Amount,
) : ComponentParams
