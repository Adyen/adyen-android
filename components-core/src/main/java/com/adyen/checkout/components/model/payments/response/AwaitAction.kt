/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/8/2020.
 */
package com.adyen.checkout.components.model.payments.response

import android.os.Parcel
import com.adyen.checkout.components.util.ActionTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

class AwaitAction(
    override var type: String? = null,
    override var paymentData: String? = null,
    override var paymentMethodType: String? = null,
) : Action() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        const val ACTION_TYPE = ActionTypes.AWAIT

        @JvmField
        val CREATOR = Creator(
            AwaitAction::class.java
        )

        @JvmField
        val SERIALIZER: Serializer<AwaitAction> = object : Serializer<AwaitAction> {
            override fun serialize(modelObject: AwaitAction): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(PAYMENT_DATA, modelObject.paymentData)
                        putOpt(PAYMENT_METHOD_TYPE, modelObject.paymentMethodType)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(RedirectAction::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): AwaitAction {
                return AwaitAction(
                    type = jsonObject.getStringOrNull(TYPE),
                    paymentData = jsonObject.getStringOrNull(PAYMENT_DATA),
                    paymentMethodType = jsonObject.getStringOrNull(PAYMENT_METHOD_TYPE),
                )
            }
        }
    }
}
