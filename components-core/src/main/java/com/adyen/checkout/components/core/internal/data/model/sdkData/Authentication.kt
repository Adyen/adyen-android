/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/10/2025.
 */

package com.adyen.checkout.components.core.internal.data.model.sdkData

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class Authentication(
    val threeDS2SdkVersion: String? = null
) : ModelObject() {

    companion object {
        private const val THREEDS2_SDK_VERSION = "threeDS2SdkVersion"

        @JvmField
        val SERIALIZER: Serializer<Authentication> = object : Serializer<Authentication> {
            override fun serialize(modelObject: Authentication): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(THREEDS2_SDK_VERSION, modelObject.threeDS2SdkVersion)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(Authentication::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): Authentication {
                return Authentication(
                    threeDS2SdkVersion = jsonObject.getStringOrNull(THREEDS2_SDK_VERSION),
                )
            }
        }
    }
}
