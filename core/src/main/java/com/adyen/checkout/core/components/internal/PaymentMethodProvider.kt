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
import androidx.annotation.VisibleForTesting
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.PaymentMethod
import com.adyen.checkout.core.components.data.model.PaymentMethodResponse
import com.adyen.checkout.core.components.data.model.StoredPaymentMethod
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import com.adyen.checkout.core.sessions.internal.model.SessionParams
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

    /**
     * Create a [PaymentComponent] via a [PaymentComponentFactory].
     *
     * @param paymentMethod The payment method to create a component for.
     * @param coroutineScope The [CoroutineScope] to be used by the component.
     * @param checkoutConfiguration The global checkout configuration.
     * @param componentParamsBundle The object which contains [CommonComponentParams] and [SessionParams].
     *
     * @return [PaymentComponent] for given payment method.
     */
    @Suppress("LongParameterList")
    fun get(
        application: Application,
        paymentMethod: PaymentMethodResponse,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
        checkoutCallbacks: CheckoutCallbacks,
    ): PaymentComponent<BasePaymentComponentState> {
        return when (paymentMethod) {
            is PaymentMethod -> {
                getPaymentComponent(
                    application = application,
                    paymentMethod = paymentMethod,
                    coroutineScope = coroutineScope,
                    analyticsManager = analyticsManager,
                    checkoutConfiguration = checkoutConfiguration,
                    componentParamsBundle = componentParamsBundle,
                    checkoutCallbacks = checkoutCallbacks,
                )
            }

            is StoredPaymentMethod -> {
                getStoredPaymentComponent(
                    application = application,
                    storedPaymentMethod = paymentMethod,
                    coroutineScope = coroutineScope,
                    analyticsManager = analyticsManager,
                    checkoutConfiguration = checkoutConfiguration,
                    componentParamsBundle = componentParamsBundle,
                    checkoutCallbacks = checkoutCallbacks,
                )
            }

            else -> {
                error("")
            }
        }
    }

    @Suppress("LongParameterList")
    private fun getPaymentComponent(
        application: Application,
        paymentMethod: PaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
        checkoutCallbacks: CheckoutCallbacks,
    ): PaymentComponent<BasePaymentComponentState> {
        val txVariant = paymentMethod.type

        @Suppress("UNCHECKED_CAST")
        return factories[txVariant]?.create(
            application = application,
            paymentMethod = paymentMethod,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            checkoutConfiguration = checkoutConfiguration,
            componentParamsBundle = componentParamsBundle,
            checkoutCallbacks = checkoutCallbacks,
        ) as? PaymentComponent<BasePaymentComponentState> ?: run {
            // TODO - Errors Propagation [COSDK-85]. Do we want to use onError() here or throw an exception?
            // TODO - We could check if a factory is not registered for a supported payment method type then throw a
            //  different exception, since that means that the module is probably not imported.
            error("Factory for payment method type: $txVariant is not registered.")
        }
    }

    @Suppress("LongParameterList")
    private fun getStoredPaymentComponent(
        application: Application,
        storedPaymentMethod: StoredPaymentMethod,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
        checkoutCallbacks: CheckoutCallbacks,
    ): PaymentComponent<BasePaymentComponentState> {
        val txVariant = storedPaymentMethod.type

        @Suppress("UNCHECKED_CAST")
        return storedFactories[txVariant]?.create(
            application = application,
            storedPaymentMethod = storedPaymentMethod,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            checkoutConfiguration = checkoutConfiguration,
            componentParamsBundle = componentParamsBundle,
            checkoutCallbacks = checkoutCallbacks,
        ) as? PaymentComponent<BasePaymentComponentState> ?: run {
            // TODO - Errors Propagation. Propagate an initialization error via onError()
            error("Factory for stored payment method type: $txVariant is not registered.")
        }
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
