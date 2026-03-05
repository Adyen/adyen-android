/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/6/2025.
 */

package com.adyen.checkout.core.components.internal

import android.app.Application
import androidx.annotation.RestrictTo
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.PaymentMethod
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import com.adyen.checkout.core.sessions.internal.model.SessionParams
import kotlinx.coroutines.CoroutineScope

/**
 * Factory interface for creating payment method components.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface PaymentComponentFactory<CS : BasePaymentComponentState, T : PaymentComponent<CS>> : ComponentFactory {

    /**
     * Creates a [PaymentComponent].
     *
     * @param paymentMethod The payment method to create a component for.
     * @param coroutineScope Coroutine Scope.
     * @param analyticsManager Analytics manager for tracking component events.
     * @param checkoutConfiguration Checkout Configuration.
     * @param componentParamsBundle The object which contains [CommonComponentParams] and [SessionParams].
     * @param checkoutCallbacks Callbacks for component events.
     *
     * @return A [PaymentComponent] instance.
     */
    @Suppress("LongParameterList")
    fun create(
        application: Application,
        paymentMethod: PaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
        checkoutCallbacks: CheckoutCallbacks,
    ): T
}
