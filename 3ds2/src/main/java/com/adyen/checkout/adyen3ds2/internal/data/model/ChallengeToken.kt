/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 10/5/2019.
 */
package com.adyen.checkout.adyen3ds2.internal.data.model

import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class ChallengeToken(
    var acsReferenceNumber: String? = null,
    var acsSignedContent: String? = null,
    var acsTransID: String? = null,
    var acsURL: String? = null,
    var messageVersion: String? = null,
    var threeDSServerTransID: String? = null
) : ModelObject() {

    companion object {
        private const val ACS_REFERENCE_NUMBER = "acsReferenceNumber"
        private const val ACS_SIGNED_CONTENT = "acsSignedContent"
        private const val ACS_TRANS_ID = "acsTransID"
        private const val ACS_URL = "acsURL"
        private const val MESSAGE_VERSION = "messageVersion"
        private const val THREEDS_SERVER_TRANS_ID = "threeDSServerTransID"

        @JvmField
        val SERIALIZER: Serializer<ChallengeToken> = object : Serializer<ChallengeToken> {
            override fun serialize(modelObject: ChallengeToken): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(ACS_REFERENCE_NUMBER, modelObject.acsReferenceNumber)
                        putOpt(ACS_SIGNED_CONTENT, modelObject.acsSignedContent)
                        putOpt(ACS_TRANS_ID, modelObject.acsTransID)
                        putOpt(ACS_URL, modelObject.acsURL)
                        putOpt(MESSAGE_VERSION, modelObject.messageVersion)
                        putOpt(THREEDS_SERVER_TRANS_ID, modelObject.threeDSServerTransID)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(ChallengeToken::class.java, e)
                }
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
