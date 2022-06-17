/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/4/2021.
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

data class SubmitFingerprintRequest(
    val encodedFingerprint: String?,
    val paymentData: String?
) : ModelObject() {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        JsonUtils.writeToParcel(parcel, SERIALIZER.serialize(this))
    }

    companion object {
        private const val FINGERPRINT = "fingerprintResult"
        private const val PAYMENT_DATA = "paymentData"

        @JvmField
        val CREATOR: Parcelable.Creator<SubmitFingerprintRequest> = Creator(SubmitFingerprintRequest::class.java)

        @JvmField
        val SERIALIZER: Serializer<SubmitFingerprintRequest> = object : Serializer<SubmitFingerprintRequest> {
            override fun serialize(modelObject: SubmitFingerprintRequest): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(FINGERPRINT, modelObject.encodedFingerprint)
                    jsonObject.putOpt(PAYMENT_DATA, modelObject.paymentData)
                } catch (e: JSONException) {
                    throw ModelSerializationException(SubmitFingerprintRequest::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): SubmitFingerprintRequest {
                return try {
                    SubmitFingerprintRequest(
                        encodedFingerprint = jsonObject.getStringOrNull(FINGERPRINT),
                        paymentData = jsonObject.getStringOrNull(PAYMENT_DATA)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SubmitFingerprintRequest::class.java, e)
                }
            }
        }
    }
}
