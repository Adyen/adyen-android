package com.adyen.checkout.sessions.model.setup

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.ModelObject
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class SessionSetupConfiguration(val enableStoreDetails: Boolean? = null) : ModelObject() {

    companion object {
        private const val ENABLE_STORE_DETAILS = "enableStoreDetails"

        @JvmField
        val SERIALIZER: Serializer<SessionSetupConfiguration> = object : Serializer<SessionSetupConfiguration> {
            override fun serialize(modelObject: SessionSetupConfiguration): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(ENABLE_STORE_DETAILS, modelObject.enableStoreDetails)
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionSetupConfiguration::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): SessionSetupConfiguration {
                return try {
                    SessionSetupConfiguration(
                        enableStoreDetails = jsonObject.optBoolean(ENABLE_STORE_DETAILS)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionSetupConfiguration::class.java, e)
                }
            }
        }
    }
}
