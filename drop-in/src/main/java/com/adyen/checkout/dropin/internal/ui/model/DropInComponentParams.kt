/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/11/2022.
 */

package com.adyen.checkout.dropin.internal.ui.model

import android.os.Bundle
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.core.Environment
import java.util.Locale

internal data class DropInComponentParams(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsParams: AnalyticsParams,
    override val isCreatedByDropIn: Boolean,
    override val amount: Amount,
    val showPreselectedStoredPaymentMethod: Boolean,
    val skipListWhenSinglePaymentMethod: Boolean,
    val isRemovingStoredPaymentMethodsEnabled: Boolean,
    val additionalDataForDropInService: Bundle?,
) : ComponentParams
