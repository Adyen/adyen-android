/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/6/2025.
 */

package com.adyen.checkout.core.components.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.ui.PaymentDelegate
import com.adyen.checkout.core.sessions.internal.model.SessionParams
import kotlinx.coroutines.CoroutineScope

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object PaymentMethodProvider {

    private val factories = mutableMapOf<String, PaymentMethodFactory<*, *>>()

    fun register(
        txVariant: String,
        factory: PaymentMethodFactory<*, *>,
    ) {
        factories[txVariant] = factory
    }

    /**
     * Create a [PaymentDelegate] via a [PaymentMethodFactory].
     *
     * @param txVariant Payment Method Type.
     * @param coroutineScope Coroutine Scope.
     * @param checkoutConfiguration Checkout Configuration.
     * @param componentSessionParams Configuration from Sessions.
     *
     * @return [PaymentDelegate] for given txVariant.
     */
    fun get(
        txVariant: String,
        coroutineScope: CoroutineScope,
        checkoutConfiguration: CheckoutConfiguration,
        componentSessionParams: SessionParams?,
    ): PaymentDelegate<BaseComponentState> {
        @Suppress("UNCHECKED_CAST")
        return factories[txVariant]?.create(
            coroutineScope = coroutineScope,
            checkoutConfiguration = checkoutConfiguration,
            componentSessionParams = componentSessionParams,
        ) as? PaymentDelegate<BaseComponentState> ?: run {
            // TODO - Errors Propagation [COSDK-85]. Propagate an initialization error via onError()
            error("Factory for payment method type: $txVariant is not registered.")
        }
    }
}
