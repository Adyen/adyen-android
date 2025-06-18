/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/6/2019.
 */
package com.adyen.checkout.components.core.action

import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class Threeds2ChallengeAction(
    override var type: String? = null,
    override var paymentData: String? = null,
    override var paymentMethodType: String? = null,
    var token: String? = null
) : BaseThreeds2Action() {

    companion object {
        const val ACTION_TYPE = ActionTypes.THREEDS2_CHALLENGE
        private const val TOKEN = "token"

        @JvmField
        val SERIALIZER: Serializer<Threeds2ChallengeAction> = object : Serializer<Threeds2ChallengeAction> {
            override fun serialize(modelObject: Threeds2ChallengeAction): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(PAYMENT_DATA, modelObject.paymentData)
                        putOpt(PAYMENT_METHOD_TYPE, modelObject.paymentMethodType)
                        putOpt(TOKEN, modelObject.token)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(Threeds2ChallengeAction::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): Threeds2ChallengeAction {
                return try {
                    Threeds2ChallengeAction(
                        token = jsonObject.getStringOrNull(TOKEN),
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
}
