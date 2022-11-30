/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/11/2022.
 */

package com.adyen.checkout.dropin

import android.os.Bundle
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.api.Environment
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
internal data class DropInComponentParams(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val isAnalyticsEnabled: Boolean,
    override val isCreatedByDropIn: Boolean,
    val amount: Amount,
    val showPreselectedStoredPaymentMethod: Boolean,
    val skipListWhenSinglePaymentMethod: Boolean,
    val isRemovingStoredPaymentMethodsEnabled: Boolean,
    val additionalDataForDropInService: Bundle?,
) : ComponentParams
