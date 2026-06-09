/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui

import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.CheckoutAdditionalCallback
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.internal.PaymentComponentFactory
import kotlinx.coroutines.CoroutineScope

internal class GooglePayFactory : PaymentComponentFactory<GooglePayComponent> {
    override fun create(
        paymentMethod: PaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        params: CheckoutParams,
        additionalCallbacks: Set<CheckoutAdditionalCallback>,
    ): GooglePayComponent {
        return GooglePayComponent()
    }
}
