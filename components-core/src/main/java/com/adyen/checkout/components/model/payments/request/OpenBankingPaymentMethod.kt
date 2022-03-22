/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */
package com.adyen.checkout.components.model.payments.request

import android.os.Parcel
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

class OpenBankingPaymentMethod(
    override var type: String? = null,
    override var issuer: String? = null,
) : IssuerListPaymentMethod() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.OPEN_BANKING

        @JvmField
        val CREATOR = Creator(OpenBankingPaymentMethod::class.java)

        @JvmField
        val SERIALIZER: Serializer<OpenBankingPaymentMethod> = object : Serializer<OpenBankingPaymentMethod> {
            override fun serialize(modelObject: OpenBankingPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(ISSUER, modelObject.issuer)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(OpenBankingPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): OpenBankingPaymentMethod {
                return OpenBankingPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    issuer = jsonObject.getStringOrNull(ISSUER),
                )
            }
        }
    }
}
