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
    val threeDSServerTransID: String? = null
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val DIRECTORY_SERVER_ID = "directoryServerId"
        private const val DIRECTORY_SERVER_PUBLIC_KEY = "directoryServerPublicKey"
        private const val THREEDS_SERVER_TRANS_ID = "threeDSServerTransID"

        @JvmField
        val CREATOR: Parcelable.Creator<FingerprintToken> = Creator(FingerprintToken::class.java)

        @JvmField
        val SERIALIZER: Serializer<FingerprintToken> = object : Serializer<FingerprintToken> {
            override fun serialize(modelObject: FingerprintToken): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(DIRECTORY_SERVER_ID, modelObject.directoryServerId)
                    jsonObject.putOpt(DIRECTORY_SERVER_PUBLIC_KEY, modelObject.directoryServerPublicKey)
                    jsonObject.putOpt(THREEDS_SERVER_TRANS_ID, modelObject.threeDSServerTransID)
                } catch (e: JSONException) {
                    throw ModelSerializationException(FingerprintToken::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): FingerprintToken {
                return try {
                    FingerprintToken(
                        directoryServerId = jsonObject.getStringOrNull(DIRECTORY_SERVER_ID),
                        directoryServerPublicKey = jsonObject.getStringOrNull(DIRECTORY_SERVER_PUBLIC_KEY),
                        threeDSServerTransID = jsonObject.getStringOrNull(THREEDS_SERVER_TRANS_ID)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(FingerprintToken::class.java, e)
                }
            }
        }
    }
}
