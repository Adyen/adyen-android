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
import com.adyen.checkout.core.model.ModelUtils
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class BinLookupResponse(
    val brands: List<Brand>? = null,
    val issuingCountryCode: String? = null,
    val requestId: String? = null
) : ModelObject() {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        JsonUtils.writeToParcel(parcel, SERIALIZER.serialize(this))
    }

    companion object {
        private const val BRANDS = "brands"
        private const val ISSUING_COUNTRY_CODE = "issuingCountryCode"
        private const val REQUEST_ID = "requestId"

        @JvmField
        val CREATOR: Parcelable.Creator<BinLookupResponse> = Creator(BinLookupResponse::class.java)

        @JvmField
        val SERIALIZER: Serializer<BinLookupResponse> = object : Serializer<BinLookupResponse> {
            override fun serialize(modelObject: BinLookupResponse): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(BRANDS, ModelUtils.serializeOptList(modelObject.brands, Brand.SERIALIZER))
                    jsonObject.putOpt(ISSUING_COUNTRY_CODE, modelObject.issuingCountryCode)
                    jsonObject.putOpt(REQUEST_ID, modelObject.requestId)
                } catch (e: JSONException) {
                    throw ModelSerializationException(BinLookupResponse::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): BinLookupResponse {
                return try {
                    BinLookupResponse(
                        brands = ModelUtils.deserializeOptList(jsonObject.optJSONArray(BRANDS), Brand.SERIALIZER),
                        issuingCountryCode = jsonObject.getStringOrNull(ISSUING_COUNTRY_CODE),
                        requestId = jsonObject.getStringOrNull(REQUEST_ID)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(BinLookupResponse::class.java, e)
                }
            }
        }
    }
}
