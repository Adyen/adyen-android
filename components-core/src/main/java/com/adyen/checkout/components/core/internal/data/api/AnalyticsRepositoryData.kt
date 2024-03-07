/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 4/3/2024.
 */

package com.adyen.checkout.components.core.internal.data.api

import android.app.Application
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.analytics.AnalyticsSource
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.util.screenWidthPixels
import java.util.Locale

// TODO: Remove this file
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class AnalyticsRepositoryData(
    val level: AnalyticsParamsLevel,
    val packageName: String,
    val locale: Locale,
    val source: AnalyticsSource,
    val clientKey: String,
    val amount: Amount?,
    val screenWidth: Int,
    val paymentMethods: List<String>,
    val sessionId: String?,
) {

    constructor(
        application: Application,
        componentParams: ComponentParams,
        paymentMethod: PaymentMethod,
        sessionId: String? = null,
    ) : this(
        application = application,
        componentParams = componentParams,
        source = AnalyticsSource.PaymentComponent(paymentMethod.type.orEmpty()),
        paymentMethodType = paymentMethod.type,
        sessionId = sessionId,
    )

    constructor(
        application: Application,
        componentParams: ComponentParams,
        storedPaymentMethod: StoredPaymentMethod,
        sessionId: String? = null,
    ) : this(
        application = application,
        componentParams = componentParams,
        source = AnalyticsSource.PaymentComponent(storedPaymentMethod.type.orEmpty()),
        paymentMethodType = storedPaymentMethod.type,
        sessionId = sessionId,
    )

    private constructor(
        application: Application,
        componentParams: ComponentParams,
        source: AnalyticsSource,
        paymentMethodType: String?,
        sessionId: String?,
    ) : this(
        level = componentParams.analyticsParams.level,
        packageName = application.packageName,
        locale = componentParams.shopperLocale,
        source = source,
        clientKey = componentParams.clientKey,
        amount = componentParams.amount,
        screenWidth = application.screenWidthPixels,
        paymentMethods = listOfNotNull(paymentMethodType),
        sessionId = sessionId,
    )
}
