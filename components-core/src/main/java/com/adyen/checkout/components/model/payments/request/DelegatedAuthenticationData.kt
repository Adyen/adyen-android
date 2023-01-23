/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 24/11/2022.
 */

package com.adyen.checkout.components.model.payments.request

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getBooleanOrNull
import com.adyen.checkout.core.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class DelegatedAuthenticationData(
    val sdkOutput: String?,
    val delegatedAuthenticationRequested: Boolean? = true
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
                    delegatedAuthenticationRequested = jsonObject.getBooleanOrNull(DELEGATED_AUTHENTICATION_REQUESTED)
                )
            }
        }
    }
}
