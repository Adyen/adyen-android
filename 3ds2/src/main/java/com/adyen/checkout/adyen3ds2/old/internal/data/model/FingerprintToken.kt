/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 7/5/2019.
 */
package com.adyen.checkout.adyen3ds2.old.internal.data.model

import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class FingerprintToken(
    val directoryServerId: String? = null,
    val directoryServerPublicKey: String? = null,
    val directoryServerRootCertificates: String? = null,
    val threeDSServerTransID: String? = null,
    val threeDSMessageVersion: String? = null,
) : ModelObject() {

    companion object {
        private const val DIRECTORY_SERVER_ID = "directoryServerId"
        private const val DIRECTORY_SERVER_PUBLIC_KEY = "directoryServerPublicKey"
        private const val DIRECTORY_SERVER_ROOT_CERTIFICATES = "directoryServerRootCertificates"
        private const val THREEDS_SERVER_TRANS_ID = "threeDSServerTransID"
        private const val THREEDS_MESSAGE_VERSION = "threeDSMessageVersion"

        @JvmField
        val SERIALIZER: Serializer<FingerprintToken> = object : Serializer<FingerprintToken> {
            override fun serialize(modelObject: FingerprintToken): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(DIRECTORY_SERVER_ID, modelObject.directoryServerId)
                        putOpt(DIRECTORY_SERVER_PUBLIC_KEY, modelObject.directoryServerPublicKey)
                        putOpt(DIRECTORY_SERVER_ROOT_CERTIFICATES, modelObject.directoryServerRootCertificates)
                        putOpt(THREEDS_SERVER_TRANS_ID, modelObject.threeDSServerTransID)
                        putOpt(THREEDS_MESSAGE_VERSION, modelObject.threeDSMessageVersion)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(FingerprintToken::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): FingerprintToken {
                return try {
                    with(jsonObject) {
                        FingerprintToken(
                            directoryServerId = getStringOrNull(DIRECTORY_SERVER_ID),
                            directoryServerPublicKey = getStringOrNull(DIRECTORY_SERVER_PUBLIC_KEY),
                            directoryServerRootCertificates = getStringOrNull(DIRECTORY_SERVER_ROOT_CERTIFICATES),
                            threeDSServerTransID = getStringOrNull(THREEDS_SERVER_TRANS_ID),
                            threeDSMessageVersion = getStringOrNull(THREEDS_MESSAGE_VERSION)
                        )
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(FingerprintToken::class.java, e)
                }
            }
        }
    }
}
