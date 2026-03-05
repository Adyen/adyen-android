/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui

import android.app.Application
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.PaymentMethod
import com.adyen.checkout.core.components.internal.PaymentComponentFactory
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayPaymentComponentState
import kotlinx.coroutines.CoroutineScope

internal class GooglePayFactory : PaymentComponentFactory<GooglePayPaymentComponentState, GooglePayComponent> {

    @Suppress("UNUSED_PARAMETER")
    override fun create(
        application: Application,
        paymentMethod: PaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
        checkoutCallbacks: CheckoutCallbacks
    ): GooglePayComponent {
        // TODO - Implement GooglePayComponent creation
        return GooglePayComponent()
    }
}
