/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/4/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.components.AdvancedCheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.error.CheckoutError

internal class AdvancedComponentRequestDispatcher(
    private val callbacks: AdvancedCheckoutCallbacks,
) : ComponentRequestDispatcher {

    override suspend fun submit(data: PaymentComponentData<*>): CheckoutResult {
        return callbacks.onSubmit(data)
    }

    override suspend fun additionalDetails(data: ActionComponentData) {
        callbacks.onAdditionalDetails(data)
    }

    override fun error(error: CheckoutError) {
        callbacks.onError(error)
    }
}
