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
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
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
     * Create a [PaymentComponent] via a [PaymentMethodFactory].
     *
     * @param txVariant The payment method type to be handled.
     * @param coroutineScope The [CoroutineScope] to be used by the component.
     * @param checkoutConfiguration The global checkout configuration.
     * @param componentSessionParams The [SessionParams] from Sessions.
     *
     * @return [PaymentComponent] for given txVariant.
     */
    fun get(
        txVariant: String,
        coroutineScope: CoroutineScope,
        checkoutConfiguration: CheckoutConfiguration,
        componentSessionParams: SessionParams?,
    ): PaymentComponent<BaseComponentState> {
        @Suppress("UNCHECKED_CAST")
        return factories[txVariant]?.create(
            coroutineScope = coroutineScope,
            checkoutConfiguration = checkoutConfiguration,
            componentSessionParams = componentSessionParams,
        ) as? PaymentComponent<BaseComponentState> ?: run {
            // TODO - Errors Propagation [COSDK-85]. Propagate an initialization error via onError()
            error("Factory for payment method type: $txVariant is not registered.")
        }
    }
}
