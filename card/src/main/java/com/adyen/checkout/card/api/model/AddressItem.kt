/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 11/3/2022.
 */

package com.adyen.checkout.card.api.model

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class AddressItem(
    val id: String? = null,
    val name: String? = null
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val ID = "id"
        private const val NAME = "name"

        @JvmField
        val CREATOR: Parcelable.Creator<AddressItem> = Creator(AddressItem::class.java)

        @JvmField
        val SERIALIZER: Serializer<AddressItem> = object : Serializer<AddressItem> {
            override fun serialize(modelObject: AddressItem): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(ID, modelObject.id)
                    jsonObject.putOpt(NAME, modelObject.name)
                } catch (e: JSONException) {
                    throw ModelSerializationException(AddressItem::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): AddressItem {
                return try {
                    AddressItem(
                        id = jsonObject.getStringOrNull(ID),
                        name = jsonObject.getStringOrNull(NAME)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(AddressItem::class.java, e)
                }
            }
        }
    }
}
