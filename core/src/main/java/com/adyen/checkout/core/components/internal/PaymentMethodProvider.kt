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
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParamsMapperData
import com.adyen.checkout.core.sessions.internal.model.SessionParams
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object PaymentMethodProvider {

    private val factories = ConcurrentHashMap<String, PaymentMethodFactory<*, *>>()

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
     * @param commonComponentParamsMapperData The object which contains [CommonComponentParams] and [SessionParams].
     *
     * @return [PaymentComponent] for given txVariant.
     */
    fun get(
        txVariant: String,
        coroutineScope: CoroutineScope,
        checkoutConfiguration: CheckoutConfiguration,
        commonComponentParamsMapperData: CommonComponentParamsMapperData,
    ): PaymentComponent<BasePaymentComponentState> {
        @Suppress("UNCHECKED_CAST")
        return factories[txVariant]?.create(
            coroutineScope = coroutineScope,
            checkoutConfiguration = checkoutConfiguration,
            commonComponentParamsMapperData = commonComponentParamsMapperData,
        ) as? PaymentComponent<BasePaymentComponentState> ?: run {
            // TODO - Errors Propagation [COSDK-85]. Propagate an initialization error via onError()
            error("Factory for payment method type: $txVariant is not registered.")
        }
    }

    /**
     * Clears all registered factories. Should only be used in tests.
     */
    @VisibleForTesting
    internal fun clear() {
        factories.clear()
    }

    /**
     * Returns the number of registered factories. Should only be used in tests.
     */
    @VisibleForTesting
    internal fun getFactoriesCount(): Int {
        return factories.size
    }
}
