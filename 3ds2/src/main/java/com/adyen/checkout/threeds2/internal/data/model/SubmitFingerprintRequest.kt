/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/4/2021.
 */

package com.adyen.checkout.threeds2.internal.data.model

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
internal data class SubmitFingerprintRequest(
    val encodedFingerprint: String?,
    val paymentData: String?
) : ModelObject() {

    companion object {
        private const val FINGERPRINT = "fingerprintResult"
        private const val PAYMENT_DATA = "paymentData"

        @JvmField
        val SERIALIZER: Serializer<SubmitFingerprintRequest> = object : Serializer<SubmitFingerprintRequest> {
            override fun serialize(modelObject: SubmitFingerprintRequest): JSONObject {
                return JSONObject().apply {
                    putOpt(FINGERPRINT, modelObject.encodedFingerprint)
                    putOpt(PAYMENT_DATA, modelObject.paymentData)
                }
            }

            override fun deserialize(jsonObject: JSONObject): SubmitFingerprintRequest {
                return SubmitFingerprintRequest(
                    encodedFingerprint = jsonObject.getStringOrNull(FINGERPRINT),
                    paymentData = jsonObject.getStringOrNull(PAYMENT_DATA)
                )
            }
        }
    }
}
