/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/6/2025.
 */

package com.adyen.checkout.core.components.internal

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object PaymentMethodProvider {

    private val factories = ConcurrentHashMap<String, PaymentComponentFactory<*, *>>()
    private val storedFactories = ConcurrentHashMap<String, StoredPaymentComponentFactory<*, *>>()

    fun register(
        txVariant: String,
        factory: ComponentFactory,
    ) {
        if (factory is PaymentComponentFactory<*, *>) {
            factories[txVariant] = factory
        }

        if (factory is StoredPaymentComponentFactory<*, *>) {
            storedFactories[txVariant] = factory
        }
    }

    @Suppress("LongParameterList")
    fun getPaymentComponent(
        paymentMethod: PaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
    ): PaymentComponent<BasePaymentComponentState>? {
        val txVariant = paymentMethod.type

        @Suppress("UNCHECKED_CAST")
        return factories[txVariant]?.create(
            paymentMethod = paymentMethod,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            checkoutConfiguration = checkoutConfiguration,
            componentParamsBundle = componentParamsBundle,
        ) as? PaymentComponent<BasePaymentComponentState>
    }

    @Suppress("LongParameterList")
    fun getStoredPaymentComponent(
        storedPaymentMethod: StoredPaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
    ): PaymentComponent<BasePaymentComponentState>? {
        val txVariant = storedPaymentMethod.type

        @Suppress("UNCHECKED_CAST")
        return storedFactories[txVariant]?.create(
            storedPaymentMethod = storedPaymentMethod,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            checkoutConfiguration = checkoutConfiguration,
            componentParamsBundle = componentParamsBundle,
        ) as? PaymentComponent<BasePaymentComponentState>
    }

    /**
     * Clears all registered factories. Should only be used in tests.
     */
    @VisibleForTesting
    internal fun clear() {
        factories.clear()
        storedFactories.clear()
    }

    /**
     * Returns the number of registered factories. Should only be used in tests.
     */
    @VisibleForTesting
    internal fun getFactoriesCount(): Int {
        return factories.size
    }

    /**
     * Returns the number of registered factories. Should only be used in tests.
     */
    @VisibleForTesting
    internal fun getStoredFactoriesCount(): Int {
        return storedFactories.size
    }
}
