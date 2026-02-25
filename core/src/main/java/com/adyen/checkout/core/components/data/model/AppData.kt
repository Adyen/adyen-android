/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 12/3/2024.
 */

package com.adyen.checkout.core.components.data.model

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.ModelObject
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class AppData(
    val id: String,
    val name: String,
) : ModelObject() {

    companion object {
        private const val ID = "id"
        private const val NAME = "name"

        @JvmField
        val SERIALIZER: Serializer<AppData> = object : Serializer<AppData> {
            override fun serialize(modelObject: AppData): JSONObject {
                return try {
                    JSONObject().apply {
                        put(ID, modelObject.id)
                        put(NAME, modelObject.name)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(AppData::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): AppData {
                return AppData(
                    id = jsonObject.getString(ID),
                    name = jsonObject.getString(NAME),
                )
            }
        }
    }
}
