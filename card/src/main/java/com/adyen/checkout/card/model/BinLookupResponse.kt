/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/1/2021.
 */

package com.adyen.checkout.card.model

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.ModelObject
import org.json.JSONObject

data class BinLookupResponse(
    val todo: String?
    // TODO: 12/01/2021 check response JSON structure
) : ModelObject() {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        JsonUtils.writeToParcel(parcel, SERIALIZER.serialize(this))
    }

    companion object {
        @JvmStatic
        val CREATOR: Parcelable.Creator<BinLookupResponse> = Creator(BinLookupResponse::class.java)

        val SERIALIZER: Serializer<BinLookupResponse> = object : Serializer<BinLookupResponse> {
            override fun serialize(modelObject: BinLookupResponse): JSONObject {
                TODO("Not yet implemented")
            }

            override fun deserialize(jsonObject: JSONObject): BinLookupResponse {
                TODO("Not yet implemented")
            }
        }
    }
}