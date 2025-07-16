/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/7/2025.
 */
package com.adyen.checkout.core.action.data

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class AwaitAction(
    override val type: String?,
    override val paymentData: String?,
    override val paymentMethodType: String?,
    val url: String?,
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
                    throw ModelSerializationException(AwaitAction::class.java, e)
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
