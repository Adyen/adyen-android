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
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.CheckoutAdditionalCallback
import com.adyen.checkout.core.components.data.model.paymentmethod.InstantPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.internal.ui.GenericPaymentComponentFactory
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object PaymentMethodProvider {

    private val factories = ConcurrentHashMap<String, PaymentComponentFactory<*>>()
    private val storedFactories = ConcurrentHashMap<String, StoredPaymentComponentFactory<*>>()

    fun register(
        txVariant: String,
        factory: ComponentFactory,
    ) {
        if (factory is PaymentComponentFactory<*>) {
            factories[txVariant] = factory
        }

        if (factory is StoredPaymentComponentFactory<*>) {
            storedFactories[txVariant] = factory
        }
    }

    fun getPaymentComponent(
        paymentMethod: PaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        params: CheckoutParams,
        additionalCallbacks: Set<CheckoutAdditionalCallback>,
    ): PaymentComponent? {
        val txVariant = paymentMethod.type

        val registeredFactory = factories[txVariant] ?: if (paymentMethod is InstantPaymentMethod) {
            GenericPaymentComponentFactory()
        } else {
            null
        }

        return registeredFactory?.create(
            paymentMethod = paymentMethod,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            params = params,
            additionalCallbacks = additionalCallbacks,
        )
    }

    fun getStoredPaymentComponent(
        storedPaymentMethod: StoredPaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        params: CheckoutParams,
    ): PaymentComponent? {
        val txVariant = storedPaymentMethod.type

        return storedFactories[txVariant]?.create(
            storedPaymentMethod = storedPaymentMethod,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            params = params,
        )
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
