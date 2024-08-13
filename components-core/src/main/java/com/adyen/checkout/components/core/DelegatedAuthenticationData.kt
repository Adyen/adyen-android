/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 7/8/2024.
 */

package com.adyen.checkout.components.core

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.getBooleanOrNull
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class DelegatedAuthenticationData(
    val sdkOutput: String?,
    val delegatedAuthenticationRequested: Boolean?
) : ModelObject() {

    companion object {
        private const val SDK_OUTPUT = "sdkOutput"
        private const val DELEGATED_AUTHENTICATION_REQUESTED = "delegatedAuthenticationRequested"

        @JvmField
        val SERIALIZER: Serializer<DelegatedAuthenticationData> = object : Serializer<DelegatedAuthenticationData> {
            override fun serialize(modelObject: DelegatedAuthenticationData): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(SDK_OUTPUT, modelObject.sdkOutput)
                        putOpt(DELEGATED_AUTHENTICATION_REQUESTED, modelObject.delegatedAuthenticationRequested)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(DelegatedAuthenticationData::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): DelegatedAuthenticationData {
                return DelegatedAuthenticationData(
                    sdkOutput = jsonObject.getStringOrNull(SDK_OUTPUT),
                    delegatedAuthenticationRequested = jsonObject.getBooleanOrNull(DELEGATED_AUTHENTICATION_REQUESTED),
                )
            }
        }
    }
}
