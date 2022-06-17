/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2021.
 */

package com.adyen.checkout.components.model.connection

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.ModelObject
import org.json.JSONException
import org.json.JSONObject

data class OrderStatusRequest(val orderData: String) : ModelObject() {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        JsonUtils.writeToParcel(parcel, SERIALIZER.serialize(this))
    }

    companion object {
        private const val ORDER_DATA = "orderData"

        @JvmField
        val CREATOR: Parcelable.Creator<OrderStatusRequest> = Creator(OrderStatusRequest::class.java)

        @JvmField
        val SERIALIZER: Serializer<OrderStatusRequest> = object : Serializer<OrderStatusRequest> {
            override fun serialize(modelObject: OrderStatusRequest): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(ORDER_DATA, modelObject.orderData)
                } catch (e: JSONException) {
                    throw ModelSerializationException(OrderStatusRequest::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): OrderStatusRequest {
                return try {
                    OrderStatusRequest(orderData = jsonObject.getString(ORDER_DATA))
                } catch (e: JSONException) {
                    throw ModelSerializationException(OrderStatusRequest::class.java, e)
                }
            }
        }
    }
}
