/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 10/5/2019.
 */
package com.adyen.checkout.adyen3ds2.model

import android.os.Parcel
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class ChallengeToken(
    var acsReferenceNumber: String? = null,
    var acsSignedContent: String? = null,
    var acsTransID: String? = null,
    var acsURL: String? = null,
    var messageVersion: String? = null,
    var threeDSServerTransID: String? = null
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val ACS_REFERENCE_NUMBER = "acsReferenceNumber"
        private const val ACS_SIGNED_CONTENT = "acsSignedContent"
        private const val ACS_TRANS_ID = "acsTransID"
        private const val ACS_URL = "acsURL"
        private const val MESSAGE_VERSION = "messageVersion"
        private const val THREEDS_SERVER_TRANS_ID = "threeDSServerTransID"

        @JvmField
        val CREATOR = Creator(ChallengeToken::class.java)

        val SERIALIZER: Serializer<ChallengeToken> = object : Serializer<ChallengeToken> {
            override fun serialize(modelObject: ChallengeToken): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(ACS_REFERENCE_NUMBER, modelObject.acsReferenceNumber)
                    jsonObject.putOpt(ACS_SIGNED_CONTENT, modelObject.acsSignedContent)
                    jsonObject.putOpt(ACS_TRANS_ID, modelObject.acsTransID)
                    jsonObject.putOpt(ACS_URL, modelObject.acsURL)
                    jsonObject.putOpt(MESSAGE_VERSION, modelObject.messageVersion)
                    jsonObject.putOpt(THREEDS_SERVER_TRANS_ID, modelObject.threeDSServerTransID)
                } catch (e: JSONException) {
                    throw ModelSerializationException(ChallengeToken::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): ChallengeToken {
                return try {
                    ChallengeToken(
                        acsReferenceNumber = jsonObject.getStringOrNull(ACS_REFERENCE_NUMBER),
                        acsSignedContent = jsonObject.getStringOrNull(ACS_SIGNED_CONTENT),
                        acsTransID = jsonObject.getStringOrNull(ACS_TRANS_ID),
                        acsURL = jsonObject.getStringOrNull(ACS_URL),
                        messageVersion = jsonObject.getStringOrNull(MESSAGE_VERSION),
                        threeDSServerTransID = jsonObject.getStringOrNull(THREEDS_SERVER_TRANS_ID)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(ChallengeToken::class.java, e)
                }
            }
        }
    }
}
