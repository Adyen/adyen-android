/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/10/2023.
 */

package com.adyen.checkout.components.core.action

import com.adyen.checkout.core.exception.ModelSerializationException
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class TwintSdkData(
    val token: String,
    val isStored: Boolean,
) : SdkData() {

    companion object {

        private const val TOKEN = "token"
        private const val IS_STORED = "isStored"

        @JvmField
        val SERIALIZER: Serializer<TwintSdkData> = object : Serializer<TwintSdkData> {
            override fun serialize(modelObject: TwintSdkData): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TOKEN, modelObject.token)
                        putOpt(IS_STORED, modelObject.isStored)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(TwintSdkData::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): TwintSdkData {
                return try {
                    TwintSdkData(
                        token = jsonObject.getString(TOKEN),
                        isStored = jsonObject.optBoolean(IS_STORED),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(TwintSdkData::class.java, e)
                }
            }
        }
    }
}
