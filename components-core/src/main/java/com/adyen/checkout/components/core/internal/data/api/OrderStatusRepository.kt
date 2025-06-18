/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2021.
 */

package com.adyen.checkout.components.core.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.data.model.OrderStatusRequest
import com.adyen.checkout.components.core.internal.data.model.OrderStatusResponse
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.core.old.internal.util.runSuspendCatching

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class OrderStatusRepository(
    private val orderStatusService: OrderStatusService,
) {

    suspend fun getOrderStatus(
        clientKey: String,
        orderData: String
    ): Result<OrderStatusResponse> = runSuspendCatching {
        adyenLog(AdyenLogLevel.DEBUG) { "Getting order status" }

        val request = OrderStatusRequest(orderData)
        orderStatusService.getOrderStatus(
            request,
            clientKey,
        )
    }
}
