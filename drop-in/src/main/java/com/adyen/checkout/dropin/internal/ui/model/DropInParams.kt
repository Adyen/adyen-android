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
import com.adyen.checkout.core.old.Environment
import java.util.Locale

internal data class DropInParams(
    val shopperLocale: Locale,
    val environment: Environment,
    val clientKey: String,
    val analyticsParams: AnalyticsParams,
    val amount: Amount?,
    val showPreselectedStoredPaymentMethod: Boolean,
    val skipListWhenSinglePaymentMethod: Boolean,
    val isRemovingStoredPaymentMethodsEnabled: Boolean,
    val additionalDataForDropInService: Bundle?,
    val overriddenPaymentMethodInformation: Map<String, DropInPaymentMethodInformation>,
)
