/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/6/2019.
 */
package com.adyen.checkout.components.core.action

import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import org.json.JSONObject

/**
 * An Action is an object from the response of the /payments API call that indicates what needs to be done to complete
 * the payment.
 * Each type of Action contains different properties, so we use polymorphism to parse which type of Action we are
 * dealing with.
 *
 * [Action.SERIALIZER] can be used to serialize and deserialize the subclasses of [Action] without having to know the
 * exact type of the subclass.
 */
abstract class Action : ModelObject() {
    abstract var type: String?
    abstract var paymentData: String?
    abstract var paymentMethodType: String?

    companion object {
        const val TYPE = "type"
        const val PAYMENT_DATA = "paymentData"
        const val PAYMENT_METHOD_TYPE = "paymentMethodType"

        @JvmField
        val SERIALIZER: Serializer<Action> = object : Serializer<Action> {
            override fun serialize(modelObject: Action): JSONObject {
                val actionType = modelObject.type
                if (actionType.isNullOrEmpty()) {
                    throw CheckoutException("Action type not found")
                }
                return getChildSerializer(actionType).serialize(modelObject)
            }

            override fun deserialize(jsonObject: JSONObject): Action {
                val actionType = jsonObject.getStringOrNull(TYPE) ?: throw CheckoutException("Action type not found")
                val serializer = getChildSerializer(actionType)
                return serializer.deserialize(jsonObject)
            }
        }

        fun getChildSerializer(actionType: String): Serializer<Action> {
            val childSerializer = when (actionType) {
                RedirectAction.ACTION_TYPE,
                ActionTypes.NATIVE_REDIRECT -> RedirectAction.SERIALIZER

                Threeds2FingerprintAction.ACTION_TYPE -> Threeds2FingerprintAction.SERIALIZER
                Threeds2ChallengeAction.ACTION_TYPE -> Threeds2ChallengeAction.SERIALIZER
                Threeds2Action.ACTION_TYPE -> Threeds2Action.SERIALIZER
                QrCodeAction.ACTION_TYPE -> QrCodeAction.SERIALIZER
                VoucherAction.ACTION_TYPE -> VoucherAction.SERIALIZER
                SdkAction.ACTION_TYPE -> SdkAction.SERIALIZER
                AwaitAction.ACTION_TYPE -> AwaitAction.SERIALIZER
                else -> throw CheckoutException("Action type not found - $actionType")
            }
            @Suppress("UNCHECKED_CAST")
            return childSerializer as Serializer<Action>
        }
    }
}
