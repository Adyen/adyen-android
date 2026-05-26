/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/5/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.components.ActionOnlyCheckoutCallbacks
import com.adyen.checkout.core.components.AdditionalDetailsResult
import com.adyen.checkout.core.error.CheckoutError

internal class ActionOnlyComponentRequestDispatcher(
    private val callbacks: ActionOnlyCheckoutCallbacks,
) : ComponentRequestDispatcher {

    override suspend fun additionalDetails(data: ActionComponentData): AdditionalDetailsResult {
        return callbacks.onAdditionalDetails.invoke(data)
    }

    override fun error(error: CheckoutError) {
        callbacks.onError.invoke(error)
    }
}
