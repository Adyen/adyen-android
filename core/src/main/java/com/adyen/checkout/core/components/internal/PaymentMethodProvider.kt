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
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.CheckoutAdditionalCallback
import com.adyen.checkout.core.components.data.model.paymentmethod.GenericPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.GenericPaymentComponentFactory
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import kotlinx.coroutines.CancellationException
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

    @Suppress("LongParameterList")
    fun getPaymentComponent(
        paymentMethod: PaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        sdkDataProvider: SdkDataProvider,
        params: CheckoutParams,
        additionalCallbacks: Set<CheckoutAdditionalCallback>,
    ): PaymentComponent? {
        return resolveFactory(paymentMethod)?.create(
            paymentMethod = paymentMethod,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            sdkDataProvider = sdkDataProvider,
            params = params,
            additionalCallbacks = additionalCallbacks,
        )
    }

    /**
     * Returns whether the given payment method can be used in the current environment.
     *
     * Payment methods without a registered factory cannot be used and return `false`. Payment
     * methods whose factory does not implement [PaymentMethodAvailabilityCheck] are always
     * available.
     */
    suspend fun isAvailable(
        paymentMethod: PaymentMethod,
        params: CheckoutParams,
    ): Boolean {
        return when (val factory = resolveFactory(paymentMethod)) {
            null -> false
            is PaymentMethodAvailabilityCheck -> runAvailabilityCheck(factory, paymentMethod, params)
            else -> true
        }
    }

    // The check is required to never throw, so any unexpected error is caught and logged.
    @Suppress("TooGenericExceptionCaught")
    private suspend fun runAvailabilityCheck(
        availabilityCheck: PaymentMethodAvailabilityCheck,
        paymentMethod: PaymentMethod,
        params: CheckoutParams,
    ): Boolean {
        return try {
            availabilityCheck.isAvailable(paymentMethod, params)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            adyenLog(AdyenLogLevel.ERROR, e) { "Availability check failed for payment method ${paymentMethod.type}." }
            false
        }
    }

    private fun resolveFactory(paymentMethod: PaymentMethod): PaymentComponentFactory<*>? {
        return factories[paymentMethod.type] ?: if (paymentMethod is GenericPaymentMethod) {
            GenericPaymentComponentFactory
        } else {
            null
        }
    }

    fun getStoredPaymentComponent(
        storedPaymentMethod: StoredPaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        sdkDataProvider: SdkDataProvider,
        params: CheckoutParams,
    ): PaymentComponent? {
        val txVariant = storedPaymentMethod.type

        return storedFactories[txVariant]?.create(
            storedPaymentMethod = storedPaymentMethod,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            sdkDataProvider = sdkDataProvider,
            params = params,
        )
    }

    /**
     * Clears all registered factories. Should only be used in tests.
     */
    @VisibleForTesting
    fun clear() {
        factories.clear()
        storedFactories.clear()
    }

    /**
     * Returns the number of registered factories. Should only be used in tests.
     */
    @VisibleForTesting
    fun getFactoriesCount(): Int {
        return factories.size
    }

    /**
     * Returns the number of registered factories. Should only be used in tests.
     */
    @VisibleForTesting
    fun getStoredFactoriesCount(): Int {
        return storedFactories.size
    }
}
