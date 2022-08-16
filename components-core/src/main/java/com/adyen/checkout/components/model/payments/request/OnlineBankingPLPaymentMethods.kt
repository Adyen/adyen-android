/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 10/8/2022.
 */

package com.adyen.checkout.components.model.payments.request

import android.os.Parcel
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

class OnlineBankingPLPaymentMethods(
    override var type: String? = null,
    override var issuer: String? = null,
) : IssuerListPaymentMethod() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.ONLINE_BANKING_PL

        @JvmField
        val CREATOR = Creator(OnlineBankingPLPaymentMethods::class.java)

        @JvmField
        val SERIALIZER: Serializer<OnlineBankingPLPaymentMethods> = object : Serializer<OnlineBankingPLPaymentMethods> {
            override fun serialize(modelObject: OnlineBankingPLPaymentMethods): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(ISSUER, modelObject.issuer)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(OnlineBankingPLPaymentMethods::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): OnlineBankingPLPaymentMethods {
                return OnlineBankingPLPaymentMethods(
                    type = jsonObject.getStringOrNull(TYPE),
                    issuer = jsonObject.getStringOrNull(ISSUER)
                )
            }
        }
    }
}
