/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2021.
 */

package com.adyen.checkout.components.repository

import com.adyen.checkout.components.api.OrderStatusConnection
import com.adyen.checkout.components.api.suspendedCall
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.connection.OrderStatusRequest
import com.adyen.checkout.components.model.connection.OrderStatusResponse
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import java.io.IOException
import org.json.JSONException

class OrderStatusRepository {

    companion object {
        private val TAG = LogUtil.getTag()
    }

    suspend fun getOrderStatus(
        configuration: Configuration,
        orderData: String
    ): OrderStatusResponse {
        Logger.d(TAG, "Getting order status")
        try {
            val request = OrderStatusRequest(orderData)
            return OrderStatusConnection(
                request,
                environment = configuration.environment,
                clientKey = configuration.clientKey
            ).suspendedCall()
        } catch (e: IOException) {
            Logger.e(TAG, "OrderStatusConnection Failed", e)
            throw CheckoutException("Unable to get order status")
        } catch (e: JSONException) {
            Logger.e(TAG, "OrderStatusConnection unexpected result", e)
            throw CheckoutException("Unable to get order status")
        }
    }
}
