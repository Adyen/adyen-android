/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/2/2023.
 */

package com.adyen.checkout.sessions.core

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.jsonToMap
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class SessionSetupConfiguration(
    val enableStoreDetails: Boolean? = null,
    val installmentOptions: Map<String, SessionSetupInstallmentOptions?>? = null
) : ModelObject() {

    companion object {
        private const val ENABLE_STORE_DETAILS = "enableStoreDetails"
        private const val INSTALLMENT_OPTIONS = "installmentOptions"

        @JvmField
        val SERIALIZER: Serializer<SessionSetupConfiguration> = object : Serializer<SessionSetupConfiguration> {
            override fun serialize(modelObject: SessionSetupConfiguration): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(ENABLE_STORE_DETAILS, modelObject.enableStoreDetails)
                        putOpt(
                            INSTALLMENT_OPTIONS,
                            modelObject.installmentOptions?.let { JSONObject(it) }
                        )
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionSetupConfiguration::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): SessionSetupConfiguration {
                return try {
                    SessionSetupConfiguration(
                        enableStoreDetails = jsonObject.optBoolean(ENABLE_STORE_DETAILS),
                        installmentOptions = jsonObject.optJSONObject(INSTALLMENT_OPTIONS)
                            ?.jsonToMap(SessionSetupInstallmentOptions.SERIALIZER)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionSetupConfiguration::class.java, e)
                }
            }
        }
    }
}
