/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/5/2019.
 */
package com.adyen.checkout.core.components.data.model

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class Item(
    var id: String? = null,
    var name: String? = null,
) : ModelObject() {

    companion object {
        private const val ID = "id"
        private const val NAME = "name"

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
