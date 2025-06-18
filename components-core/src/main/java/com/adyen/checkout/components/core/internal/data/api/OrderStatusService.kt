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
import com.adyen.checkout.core.old.DispatcherProvider
import com.adyen.checkout.core.old.internal.data.api.HttpClient
import com.adyen.checkout.core.old.internal.data.api.post
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class OrderStatusService(
    private val httpClient: HttpClient,
    private val coroutineDispatcher: CoroutineDispatcher = DispatcherProvider.IO,
) {

    internal suspend fun getOrderStatus(
        request: OrderStatusRequest,
        clientKey: String
    ): OrderStatusResponse = withContext(coroutineDispatcher) {
        httpClient.post(
            path = "v1/order/status",
            queryParameters = mapOf("clientKey" to clientKey),
            body = request,
            requestSerializer = OrderStatusRequest.SERIALIZER,
            responseSerializer = OrderStatusResponse.SERIALIZER,
        )
    }
}
