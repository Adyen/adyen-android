/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/5/2019.
 */
package com.adyen.checkout.components.model.paymentmethods

import android.os.Parcel
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class Item(
    var id: String? = null,
    var name: String? = null,
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val ID = "id"
        private const val NAME = "name"

        @JvmField
        val CREATOR = Creator(Item::class.java)

        @JvmField
        val SERIALIZER: Serializer<Item> = object : Serializer<Item> {
            override fun serialize(modelObject: Item): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(ID, modelObject.id)
                        putOpt(NAME, modelObject.name)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(Item::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): Item {
                return Item(
                    id = jsonObject.getStringOrNull(ID),
                    name = jsonObject.getStringOrNull(NAME),
                )
            }
        }
    }
}
