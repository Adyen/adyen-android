/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 30/7/2019.
 */
package com.adyen.checkout.googlepay.model

import android.os.Parcel
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class MerchantInfo(
    var merchantName: String? = null,
    var merchantId: String? = null,
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val MERCHANT_NAME = "merchantName"
        private const val MERCHANT_ID = "merchantId"

        @JvmField
        val CREATOR = Creator(MerchantInfo::class.java)

        @JvmField
        val SERIALIZER: Serializer<MerchantInfo> = object : Serializer<MerchantInfo> {
            override fun serialize(modelObject: MerchantInfo): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(MERCHANT_NAME, modelObject.merchantName)
                        putOpt(MERCHANT_ID, modelObject.merchantId)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(MerchantInfo::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject) = MerchantInfo(
                merchantName = jsonObject.getStringOrNull(MERCHANT_NAME),
                merchantId = jsonObject.getStringOrNull(MERCHANT_ID),
            )
        }
    }
}
