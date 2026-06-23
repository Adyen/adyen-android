/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/6/2026.
 */

package com.adyen.checkout.core.components.internal.ui

import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.CheckoutAdditionalCallback
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.internal.PaymentComponentFactory
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import kotlinx.coroutines.CoroutineScope

internal object GenericPaymentComponentFactory : PaymentComponentFactory<GenericPaymentComponent> {

    override fun create(
        paymentMethod: PaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        sdkDataProvider: SdkDataProvider,
        params: CheckoutParams,
        additionalCallbacks: Set<CheckoutAdditionalCallback>
    ): GenericPaymentComponent {
        return GenericPaymentComponent(
            analyticsManager = analyticsManager,
            paymentMethodType = paymentMethod.type,
            sdkDataProvider = sdkDataProvider,
        )
    }
}
