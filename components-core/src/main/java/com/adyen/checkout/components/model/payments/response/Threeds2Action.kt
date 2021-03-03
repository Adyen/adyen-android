/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/12/2020.
 */
package com.adyen.checkout.components.model.payments.response

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.util.ActionTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

class Threeds2Action(
    val token: String? = null,
    val subtype: String? = null,
    val authorisationToken: String? = null
) : Action() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        const val ACTION_TYPE = ActionTypes.THREEDS2

        private const val TOKEN = "token"
        private const val SUBTYPE = "subtype"
        private const val AUTHORISATION_TOKEN = "authorisationToken"

        @JvmField
        val CREATOR: Parcelable.Creator<Threeds2Action> = Creator(Threeds2Action::class.java)

        @JvmField
        val SERIALIZER: Serializer<Threeds2Action> = object : Serializer<Threeds2Action> {
            override fun serialize(modelObject: Threeds2Action): JSONObject {
                val jsonObject = JSONObject()
                try {
                    // Get parameters from parent class
                    jsonObject.putOpt(TYPE, modelObject.type)
                    jsonObject.putOpt(PAYMENT_DATA, modelObject.paymentData)
                    jsonObject.putOpt(PAYMENT_METHOD_TYPE, modelObject.paymentMethodType)

                    jsonObject.putOpt(TOKEN, modelObject.token)
                    jsonObject.putOpt(SUBTYPE, modelObject.subtype)
                    jsonObject.putOpt(AUTHORISATION_TOKEN, modelObject.authorisationToken)
                } catch (e: JSONException) {
                    throw ModelSerializationException(Threeds2Action::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): Threeds2Action {
                return try {
                    Threeds2Action(
                        token = jsonObject.getStringOrNull(TOKEN),
                        subtype = jsonObject.getStringOrNull(SUBTYPE),
                        authorisationToken = jsonObject.getStringOrNull(AUTHORISATION_TOKEN)
                    ).apply {
                        type = jsonObject.getStringOrNull(TYPE)
                        paymentData = jsonObject.getStringOrNull(PAYMENT_DATA)
                        paymentMethodType = jsonObject.getStringOrNull(PAYMENT_METHOD_TYPE)
                    }
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
