/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/12/2020.
 */
package com.adyen.checkout.core.action.data

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
class Threeds2Action(
    override val type: String? = null,
    override val paymentData: String? = null,
    override val paymentMethodType: String? = null,
    val token: String? = null,
    val subtype: String? = null,
    val authorisationToken: String? = null
) : Action() {

    companion object {
        const val ACTION_TYPE = ActionTypes.THREEDS2

        private const val TOKEN = "token"
        private const val SUBTYPE = "subtype"
        private const val AUTHORISATION_TOKEN = "authorisationToken"

        @JvmField
        val SERIALIZER: Serializer<Threeds2Action> = object : Serializer<Threeds2Action> {
            override fun serialize(modelObject: Threeds2Action): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(PAYMENT_DATA, modelObject.paymentData)
                        putOpt(PAYMENT_METHOD_TYPE, modelObject.paymentMethodType)
                        putOpt(TOKEN, modelObject.token)
                        putOpt(SUBTYPE, modelObject.subtype)
                        putOpt(AUTHORISATION_TOKEN, modelObject.authorisationToken)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(Threeds2Action::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): Threeds2Action {
                return try {
                    Threeds2Action(
                        token = jsonObject.getStringOrNull(TOKEN),
                        subtype = jsonObject.getStringOrNull(SUBTYPE),
                        authorisationToken = jsonObject.getStringOrNull(AUTHORISATION_TOKEN),
                        type = jsonObject.getStringOrNull(TYPE),
                        paymentData = jsonObject.getStringOrNull(PAYMENT_DATA),
                        paymentMethodType = jsonObject.getStringOrNull(PAYMENT_METHOD_TYPE),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(Threeds2Action::class.java, e)
                }
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
