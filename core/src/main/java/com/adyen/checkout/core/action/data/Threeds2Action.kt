/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/12/2020.
 */
package com.adyen.checkout.core.action.data

import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class Threeds2Action(
    override val type: String,
    override val paymentData: String?,
    override val paymentMethodType: String?,
    val token: String?,
    val subtype: String?,
    val authorisationToken: String?,
) : Action() {

    companion object {
        const val ACTION_TYPE = ActionTypes.THREEDS2

        private const val TOKEN = "token"
        private const val SUBTYPE = "subtype"
        private const val AUTHORISATION_TOKEN = "authorisationToken"

        @JvmField
        val SERIALIZER: Serializer<Threeds2Action> = object : Serializer<Threeds2Action> {
            override fun serialize(modelObject: Threeds2Action): JSONObject {
                return JSONObject().apply {
                    putOpt(TYPE, modelObject.type)
                    putOpt(PAYMENT_DATA, modelObject.paymentData)
                    putOpt(PAYMENT_METHOD_TYPE, modelObject.paymentMethodType)
                    putOpt(TOKEN, modelObject.token)
                    putOpt(SUBTYPE, modelObject.subtype)
                    putOpt(AUTHORISATION_TOKEN, modelObject.authorisationToken)
                }
            }

            override fun deserialize(jsonObject: JSONObject): Threeds2Action {
                return Threeds2Action(
                    type = jsonObject.getString(TYPE),
                    token = jsonObject.getStringOrNull(TOKEN),
                    subtype = jsonObject.getStringOrNull(SUBTYPE),
                    authorisationToken = jsonObject.getStringOrNull(AUTHORISATION_TOKEN),
                    paymentData = jsonObject.getStringOrNull(PAYMENT_DATA),
                    paymentMethodType = jsonObject.getStringOrNull(PAYMENT_METHOD_TYPE),
                )
            }
        }
    }

    enum class SubType(val value: String) {
        FINGERPRINT("fingerprint"),
        CHALLENGE("challenge");

        companion object {
            @JvmStatic
            fun parse(value: String): SubType {
                return when (value) {
                    FINGERPRINT.value -> FINGERPRINT
                    CHALLENGE.value -> CHALLENGE
                    else -> throw IllegalArgumentException("No Subtype matches the value of: $value")
                }
            }
        }
    }
}
