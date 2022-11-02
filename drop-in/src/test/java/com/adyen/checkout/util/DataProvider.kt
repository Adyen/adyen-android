/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 28/10/2022.
 */

package com.adyen.checkout.util

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.components.model.connection.OrderStatusResponse
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.dropin.ui.order.OrderModel
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

internal object DataProvider {

    internal fun getPaymentMethodsList(): List<PaymentMethod> = getPaymentMethodResponse().paymentMethods ?: emptyList()

    internal fun getStoredPaymentMethods(): List<StoredPaymentMethod> =
        getPaymentMethodResponse().storedPaymentMethods ?: emptyList()

    internal fun getOrder() = getOrderStatusResponse().mapToModel()

    @Throws(IOException::class, JSONException::class)
    private fun getPaymentMethodResponse(): PaymentMethodsApiResponse {
        return PaymentMethodsApiResponse.SERIALIZER.deserialize(
            JSONObject(readFileWithNewLineFromResources("PaymentMethodsApiResponse.json"))
        )
    }

    @Throws(IOException::class, JSONException::class)
    private fun getOrderStatusResponse(): OrderStatusResponse {
        return OrderStatusResponse.SERIALIZER.deserialize(
            JSONObject(readFileWithNewLineFromResources("Order.json"))
        )
    }

    private fun OrderStatusResponse.mapToModel() = OrderModel(
        orderData = "order_data",
        pspReference = "pspRedqwerty",
        remainingAmount = remainingAmount,
        paymentMethods = paymentMethods
    )

    @Throws(IOException::class)
    private fun readFileWithNewLineFromResources(fileName: String): String {
        return getInputStreamFromResource(fileName)?.bufferedReader()
            .use { bufferReader -> bufferReader?.readText() } ?: ""
    }

    private fun getInputStreamFromResource(fileName: String) = javaClass.classLoader?.getResourceAsStream(fileName)
}
