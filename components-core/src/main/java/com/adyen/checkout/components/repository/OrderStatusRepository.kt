/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2021.
 */

package com.adyen.checkout.components.repository

import com.adyen.checkout.components.api.OrderStatusService
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.connection.OrderStatusRequest
import com.adyen.checkout.components.model.connection.OrderStatusResponse
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.util.runSuspendCatching

class OrderStatusRepository {

    suspend fun getOrderStatus(
        configuration: Configuration,
        orderData: String
    ): Result<OrderStatusResponse> = runSuspendCatching {
        Logger.d(TAG, "Getting order status")

        val request = OrderStatusRequest(orderData)
        OrderStatusService(configuration.environment).getOrderStatus(
            request,
            configuration.clientKey
        )
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
