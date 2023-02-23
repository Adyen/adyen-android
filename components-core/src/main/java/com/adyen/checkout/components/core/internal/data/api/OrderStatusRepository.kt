/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2021.
 */

package com.adyen.checkout.components.core.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.internal.data.model.OrderStatusRequest
import com.adyen.checkout.components.core.internal.data.model.OrderStatusResponse
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.core.internal.util.runSuspendCatching

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class OrderStatusRepository(
    private val orderStatusService: OrderStatusService,
) {

    suspend fun getOrderStatus(
        configuration: Configuration,
        orderData: String
    ): Result<OrderStatusResponse> = runSuspendCatching {
        Logger.d(TAG, "Getting order status")

        val request = OrderStatusRequest(orderData)
        orderStatusService.getOrderStatus(
            request,
            configuration.clientKey
        )
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
