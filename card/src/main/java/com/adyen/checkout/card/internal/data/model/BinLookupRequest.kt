/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.data.model

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.JsonUtils
import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import com.adyen.checkout.core.common.internal.model.optStringList
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class BinLookupRequest(
    val encryptedBin: String? = null,
    val requestId: String? = null,
    val supportedBrands: List<String>? = null,
    val type: String? = null,
) : ModelObject() {

    companion object {
        private const val ENCRYPTED_BIN = "encryptedBin"
        private const val REQUEST_ID = "requestId"
        private const val SUPPORTED_BRANDS = "supportedBrands"
        private const val TYPE = "type"

        @JvmField
        val SERIALIZER: Serializer<BinLookupRequest> = object : Serializer<BinLookupRequest> {
            override fun serialize(modelObject: BinLookupRequest): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(ENCRYPTED_BIN, modelObject.encryptedBin)
                    jsonObject.putOpt(REQUEST_ID, modelObject.requestId)
                    jsonObject.putOpt(SUPPORTED_BRANDS, JsonUtils.serializeOptStringList(modelObject.supportedBrands))
                    jsonObject.putOpt(TYPE, modelObject.type)
                } catch (e: JSONException) {
                    throw ModelSerializationException(BinLookupRequest::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): BinLookupRequest {
                return try {
                    BinLookupRequest(
                        encryptedBin = jsonObject.getStringOrNull(ENCRYPTED_BIN),
                        requestId = jsonObject.getStringOrNull(REQUEST_ID),
                        supportedBrands = jsonObject.optStringList(SUPPORTED_BRANDS),
                        type = jsonObject.getStringOrNull(TYPE),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(BinLookupRequest::class.java, e)
                }
            }
        }
    }
}
