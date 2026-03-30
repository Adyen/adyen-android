/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 12/3/2024.
 */

package com.adyen.checkout.components.core

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.internal.data.model.ModelUtils.serializeOpt
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class AppData(
    val id: String? = null,
    val name: String? = null,
    val appIdentifierInfo: AppIdentifierInfo? = null,
) : ModelObject() {

    companion object {
        private const val ID = "id"
        private const val NAME = "name"
        private const val APP_IDENTIFIER_INFO = "appIdentifierInfo"

        @JvmField
        val SERIALIZER: Serializer<AppData> = object : Serializer<AppData> {
            override fun serialize(modelObject: AppData): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(ID, modelObject.id)
                        putOpt(NAME, modelObject.name)
                        putOpt(
                            APP_IDENTIFIER_INFO,
                            serializeOpt(modelObject.appIdentifierInfo, AppIdentifierInfo.SERIALIZER),
                        )
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(AppData::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): AppData {
                return AppData(
                    id = jsonObject.getStringOrNull(ID),
                    name = jsonObject.getStringOrNull(NAME),
                    appIdentifierInfo = deserializeOpt(
                        jsonObject.getJSONObject(APP_IDENTIFIER_INFO),
                        AppIdentifierInfo.SERIALIZER,
                    ),
                )
            }
        }
    }
}
