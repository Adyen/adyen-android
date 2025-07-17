/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/7/2025.
 */
package com.adyen.checkout.core.action.data

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getStringOrNull
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
    abstract val type: String?
    abstract val paymentData: String?
    abstract val paymentMethodType: String?

    companion object {
        const val TYPE = "type"
        const val PAYMENT_DATA = "paymentData"
        const val PAYMENT_METHOD_TYPE = "paymentMethodType"

        @Suppress("TooGenericExceptionThrown")
        @JvmField
        val SERIALIZER: Serializer<Action> = object : Serializer<Action> {
            override fun serialize(modelObject: Action): JSONObject {
                val actionType = modelObject.type
                if (actionType.isNullOrEmpty()) {
                    // TODO - Error Propagation
                    // throw CheckoutException("Action type not found")
                    throw RuntimeException("Action type not found")
                }
                return getChildSerializer(actionType).serialize(modelObject)
            }

            override fun deserialize(jsonObject: JSONObject): Action {
                val actionType = jsonObject.getStringOrNull(TYPE)
                    // TODO - Error Propagation
                    // ?: throw CheckoutException("Action type not found")
                    ?: throw RuntimeException("Action type not found")
                val serializer = getChildSerializer(actionType)
                return serializer.deserialize(jsonObject)
            }
        }

        @Suppress("TooGenericExceptionThrown")
        fun getChildSerializer(actionType: String): Serializer<Action> {
            val childSerializer = when (actionType) {
                // TODO - Investigate if it is possible to move `getChildSerializer` to specific modules
                AwaitAction.ACTION_TYPE -> AwaitAction.SERIALIZER
                else ->
                    // TODO - Error Propagation
                    // throw CheckoutException("Action type not found - $actionType")
                    throw RuntimeException("Action type not found - $actionType")
            }
            @Suppress("UNCHECKED_CAST")
            return childSerializer as Serializer<Action>
        }
    }
}
