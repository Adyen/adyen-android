/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/6/2025.
 */

package com.adyen.checkout.core.internal

import com.adyen.checkout.core.CheckoutConfiguration
import com.adyen.checkout.core.internal.ui.PaymentDelegate
import com.adyen.checkout.core.sessions.CheckoutSession
import kotlinx.coroutines.CoroutineScope

internal object PaymentMethodProvider {

    private val factories =
        mutableMapOf<String, PaymentMethodFactory<BaseComponentState, PaymentDelegate<BaseComponentState>>>()

    fun register(
        txVariant: String,
        factory: PaymentMethodFactory<BaseComponentState, PaymentDelegate<BaseComponentState>>
    ) {
        factories[txVariant] = factory
    }

    /**
     * Advanced
     */
    fun get(
        txVariant: String,
        coroutineScope: CoroutineScope,
        checkoutConfiguration: CheckoutConfiguration
    ): PaymentDelegate<BaseComponentState> {
        return factories[txVariant]?.create(
            coroutineScope = coroutineScope,
            checkoutConfiguration = checkoutConfiguration,
        ) ?: run {
            // TODO - Error propagation [COSDK-85]. Propagate an initialization error via onError()
            error("Factory for payment method type: $txVariant is not registered.")
        }
    }

    /**
     * Sessions
     */
    fun get(
        txVariant: String,
        coroutineScope: CoroutineScope,
        checkoutSession: CheckoutSession,
        checkoutConfiguration: CheckoutConfiguration
    ): PaymentDelegate<BaseComponentState> {
        return factories[txVariant]?.create(
            coroutineScope = coroutineScope,
            checkoutSession = checkoutSession,
            checkoutConfiguration = checkoutConfiguration,
        ) ?: run {
            // TODO - Errors Propagation [COSDK-85]. Propagate an initialization error via onError()
            error("Factory for payment method type: $txVariant is not registered.")
        }
    }
}
