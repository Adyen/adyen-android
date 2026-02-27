/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 25/2/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Payment method model for BLIK.
 */
@Parcelize
data class BLIKPaymentMethod(
    override val type: String,
    override val name: String,
) : PaymentMethod() {

    companion object {
        @JvmField
        val SERIALIZER: Serializer<BLIKPaymentMethod> =
            object : Serializer<BLIKPaymentMethod> {
                override fun serialize(modelObject: BLIKPaymentMethod): JSONObject {
                    return try {
                        JSONObject().apply {
                            put(TYPE, modelObject.type)
                            put(NAME, modelObject.name)
                        }
                    } catch (e: JSONException) {
                        throw ModelSerializationException(BLIKPaymentMethod::class.java, e)
                    }
                }

                override fun deserialize(jsonObject: JSONObject): BLIKPaymentMethod {
                    return try {
                        BLIKPaymentMethod(
                            type = jsonObject.getString(TYPE),
                            name = jsonObject.getString(NAME),
                        )
                    } catch (e: JSONException) {
                        throw ModelSerializationException(BLIKPaymentMethod::class.java, e)
                    }
                }
            }
    }
}
