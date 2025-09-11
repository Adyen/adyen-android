/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 30/7/2019.
 */
package com.adyen.checkout.googlepay

import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.ModelUtils
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Pass this object to [GooglePayConfiguration.merchantInfo] to set information about the merchant requesting the
 * payment.
 *
 * @param merchantName The name of the merchant.
 * @param merchantId The id of the merchant.
 * @param softwareInfo Information associated with the caller of the request.
 */
@Parcelize
data class MerchantInfo(
    var merchantName: String? = null,
    var merchantId: String? = null,
    var softwareInfo: SoftwareInfo? = null,
) : ModelObject() {

    companion object {
        private const val MERCHANT_NAME = "merchantName"
        private const val MERCHANT_ID = "merchantId"
        private const val SOFTWARE_INFO = "softwareInfo"

        @JvmField
        val SERIALIZER: Serializer<MerchantInfo> = object : Serializer<MerchantInfo> {
            override fun serialize(modelObject: MerchantInfo): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(MERCHANT_NAME, modelObject.merchantName)
                        putOpt(MERCHANT_ID, modelObject.merchantId)
                        putOpt(
                            SOFTWARE_INFO,
                            ModelUtils.serializeOpt(modelObject.softwareInfo, SoftwareInfo.SERIALIZER),
                        )
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(MerchantInfo::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject) = MerchantInfo(
                merchantName = jsonObject.getStringOrNull(MERCHANT_NAME),
                merchantId = jsonObject.getStringOrNull(MERCHANT_ID),
                softwareInfo = ModelUtils.deserializeOpt(
                    jsonObject.optJSONObject(SOFTWARE_INFO),
                    SoftwareInfo.SERIALIZER,
                ),
            )
        }
    }
}
