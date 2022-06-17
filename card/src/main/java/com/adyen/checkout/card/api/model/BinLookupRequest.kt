/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/1/2021.
 */

package com.adyen.checkout.card.api.model

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getStringOrNull
import com.adyen.checkout.core.model.optStringList
import org.json.JSONException
import org.json.JSONObject

data class BinLookupRequest(
    val encryptedBin: String? = null,
    val requestId: String? = null,
    val supportedBrands: List<String>? = null
) : ModelObject() {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        JsonUtils.writeToParcel(parcel, SERIALIZER.serialize(this))
    }

    companion object {
        private const val ENCRYPTED_BIN = "encryptedBin"
        private const val REQUEST_ID = "requestId"
        private const val SUPPORTED_BRANDS = "supportedBrands"

        @JvmField
        val CREATOR: Parcelable.Creator<BinLookupRequest> = Creator(BinLookupRequest::class.java)

        @JvmField
        val SERIALIZER: Serializer<BinLookupRequest> = object : Serializer<BinLookupRequest> {
            override fun serialize(modelObject: BinLookupRequest): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(ENCRYPTED_BIN, modelObject.encryptedBin)
                    jsonObject.putOpt(REQUEST_ID, modelObject.requestId)
                    jsonObject.putOpt(SUPPORTED_BRANDS, JsonUtils.serializeOptStringList(modelObject.supportedBrands))
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
                        supportedBrands = jsonObject.optStringList(SUPPORTED_BRANDS)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(BinLookupRequest::class.java, e)
                }
            }
        }
    }
}
