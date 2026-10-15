/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/10/2026.
 */

package com.adyen.checkout.dropin.internal.data.model

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
internal data class DisableTokenRequest(
    val sessionData: String,
    val storedPaymentMethodId: String,
) : ModelObject() {

    companion object {
        private const val SESSION_DATA = "sessionData"
        private const val STORED_PAYMENT_METHOD_ID = "storedPaymentMethodId"

        @JvmField
        val SERIALIZER: Serializer<DisableTokenRequest> = object : Serializer<DisableTokenRequest> {
            override fun serialize(modelObject: DisableTokenRequest): JSONObject {
                return JSONObject().apply {
                    putOpt(SESSION_DATA, modelObject.sessionData)
                    putOpt(STORED_PAYMENT_METHOD_ID, modelObject.storedPaymentMethodId)
                }
            }

            override fun deserialize(jsonObject: JSONObject): DisableTokenRequest {
                return DisableTokenRequest(
                    sessionData = jsonObject.getStringOrNull(SESSION_DATA).orEmpty(),
                    storedPaymentMethodId = jsonObject.getStringOrNull(STORED_PAYMENT_METHOD_ID).orEmpty(),
                )
            }
        }
    }
}
