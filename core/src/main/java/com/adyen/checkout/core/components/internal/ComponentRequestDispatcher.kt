/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/4/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.error.CheckoutError

internal interface ComponentRequestDispatcher {

    suspend fun submit(data: PaymentComponentData<*>): CheckoutResult

    suspend fun additionalDetails(data: ActionComponentData)

    fun error(error: CheckoutError)
}
