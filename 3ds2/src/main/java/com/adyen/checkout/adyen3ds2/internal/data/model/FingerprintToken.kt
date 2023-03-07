/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 7/5/2019.
 */
package com.adyen.checkout.adyen3ds2.internal.data.model

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class FingerprintToken(
    val directoryServerId: String? = null,
    val directoryServerPublicKey: String? = null,
    val threeDSServerTransID: String? = null,
    val threeDSMessageVersion: String? = null,
    val delegatedAuthenticationSdkInput: String? = null
) : ModelObject() {

    companion object {
        private const val DIRECTORY_SERVER_ID = "directoryServerId"
        private const val DIRECTORY_SERVER_PUBLIC_KEY = "directoryServerPublicKey"
        private const val THREEDS_SERVER_TRANS_ID = "threeDSServerTransID"
        private const val THREEDS_MESSAGE_VERSION = "threeDSMessageVersion"
        private const val DELEGATED_AUTHENTICATION_SDK_INPUT = "delegatedAuthenticationSDKInput"

        @JvmField
        val SERIALIZER: Serializer<FingerprintToken> = object : Serializer<FingerprintToken> {
            override fun serialize(modelObject: FingerprintToken): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(DIRECTORY_SERVER_ID, modelObject.directoryServerId)
                        putOpt(DIRECTORY_SERVER_PUBLIC_KEY, modelObject.directoryServerPublicKey)
                        putOpt(THREEDS_SERVER_TRANS_ID, modelObject.threeDSServerTransID)
                        putOpt(THREEDS_MESSAGE_VERSION, modelObject.threeDSMessageVersion)
                        putOpt(DELEGATED_AUTHENTICATION_SDK_INPUT, modelObject.delegatedAuthenticationSdkInput)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(FingerprintToken::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): FingerprintToken {
                return try {
                    FingerprintToken(
                        directoryServerId = jsonObject.getStringOrNull(DIRECTORY_SERVER_ID),
                        directoryServerPublicKey = jsonObject.getStringOrNull(DIRECTORY_SERVER_PUBLIC_KEY),
                        threeDSServerTransID = jsonObject.getStringOrNull(THREEDS_SERVER_TRANS_ID),
                        threeDSMessageVersion = jsonObject.getStringOrNull(THREEDS_MESSAGE_VERSION),
                        delegatedAuthenticationSdkInput = jsonObject.getStringOrNull(DELEGATED_AUTHENTICATION_SDK_INPUT)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(FingerprintToken::class.java, e)
                }
            }
        }
    }
}
