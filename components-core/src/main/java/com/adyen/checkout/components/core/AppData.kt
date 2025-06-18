/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 12/3/2024.
 */

package com.adyen.checkout.components.core

import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class AppData(
    val id: String? = null,
    val name: String? = null,
) : ModelObject() {

    companion object {
        private const val ID = "id"
        private const val NAME = "name"

        @JvmField
        val SERIALIZER: Serializer<AppData> = object : Serializer<AppData> {
            override fun serialize(modelObject: AppData): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(ID, modelObject.id)
                        putOpt(NAME, modelObject.name)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(AppData::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): AppData {
                return AppData(
                    id = jsonObject.getStringOrNull(ID),
                    name = jsonObject.getStringOrNull(NAME),
                )
            }
        }
    }
}
