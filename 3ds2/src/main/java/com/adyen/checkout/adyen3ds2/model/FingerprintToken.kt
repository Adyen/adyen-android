/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 7/5/2019.
 */
package com.adyen.checkout.adyen3ds2.model

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class FingerprintToken(
    val directoryServerId: String? = null,
    val directoryServerPublicKey: String? = null,
    val threeDSServerTransID: String? = null,
    val threeDSMessageVersion: String? = null
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val DIRECTORY_SERVER_ID = "directoryServerId"
        private const val DIRECTORY_SERVER_PUBLIC_KEY = "directoryServerPublicKey"
        private const val THREEDS_SERVER_TRANS_ID = "threeDSServerTransID"
        private const val THREEDS_MESSAGE_VERSION = "threeDSMessageVersion"

        @JvmField
        val CREATOR: Parcelable.Creator<FingerprintToken> = Creator(FingerprintToken::class.java)

        @JvmField
        val SERIALIZER: Serializer<FingerprintToken> = object : Serializer<FingerprintToken> {
            override fun serialize(modelObject: FingerprintToken): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(DIRECTORY_SERVER_ID, modelObject.directoryServerId)
                        putOpt(DIRECTORY_SERVER_PUBLIC_KEY, modelObject.directoryServerPublicKey)
                        putOpt(THREEDS_SERVER_TRANS_ID, modelObject.threeDSServerTransID)
                        putOpt(THREEDS_MESSAGE_VERSION, modelObject.threeDSMessageVersion)
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
                        threeDSMessageVersion = jsonObject.getStringOrNull(THREEDS_MESSAGE_VERSION)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(FingerprintToken::class.java, e)
                }
            }
        }
    }
}
