/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/8/2020.
 */
package com.adyen.checkout.components.core.action

import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
class AwaitAction(
    override var type: String? = null,
    override var paymentData: String? = null,
    override var paymentMethodType: String? = null,
    var url: String? = null,
) : Action() {

    companion object {
        const val ACTION_TYPE = ActionTypes.AWAIT
        private const val URL = "url"

        @JvmField
        val SERIALIZER: Serializer<AwaitAction> = object : Serializer<AwaitAction> {
            override fun serialize(modelObject: AwaitAction): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(PAYMENT_DATA, modelObject.paymentData)
                        putOpt(PAYMENT_METHOD_TYPE, modelObject.paymentMethodType)
                        putOpt(URL, modelObject.url)
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
                    url = jsonObject.getStringOrNull(URL),
                )
            }
        }
    }
}
