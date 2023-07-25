/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 25/7/2023.
 */

package com.adyen.checkout.components.core.internal.data.api

import android.app.Application
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSource
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class AnalyticsRepositoryData(
    val level: AnalyticsParamsLevel,
    val packageName: String,
    val locale: Locale,
    val source: AnalyticsSource,
    val clientKey: String,
) {
    constructor(
        application: Application,
        componentParams: ComponentParams,
        paymentMethod: PaymentMethod,
    ) : this(
        level = componentParams.analyticsParams.level,
        packageName = application.packageName,
        locale = componentParams.shopperLocale,
        source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, paymentMethod),
        clientKey = componentParams.clientKey,
    )

    constructor(
        application: Application,
        componentParams: ComponentParams,
        storedPaymentMethod: StoredPaymentMethod,
    ) : this(
        level = componentParams.analyticsParams.level,
        packageName = application.packageName,
        locale = componentParams.shopperLocale,
        source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, storedPaymentMethod),
        clientKey = componentParams.clientKey,
    )
}
