/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.data.model

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.ModelUtils
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
internal data class BinLookupResponse(
    val brands: List<Brand>? = null,
    val issuingCountryCode: String? = null,
    val requestId: String? = null
) : ModelObject() {

    companion object {
        private const val BRANDS = "brands"
        private const val ISSUING_COUNTRY_CODE = "issuingCountryCode"
        private const val REQUEST_ID = "requestId"

        @JvmField
        val SERIALIZER: Serializer<BinLookupResponse> = object : Serializer<BinLookupResponse> {
            override fun serialize(modelObject: BinLookupResponse): JSONObject {
                return JSONObject().apply {
                    putOpt(BRANDS, ModelUtils.serializeOptList(modelObject.brands, Brand.SERIALIZER))
                    putOpt(ISSUING_COUNTRY_CODE, modelObject.issuingCountryCode)
                    putOpt(REQUEST_ID, modelObject.requestId)
                }
            }

            override fun deserialize(jsonObject: JSONObject): BinLookupResponse {
                return BinLookupResponse(
                    brands = ModelUtils.deserializeOptList(jsonObject.optJSONArray(BRANDS), Brand.SERIALIZER),
                    issuingCountryCode = jsonObject.getStringOrNull(ISSUING_COUNTRY_CODE),
                    requestId = jsonObject.getStringOrNull(REQUEST_ID)
                )
            }
        }
    }
}
