/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/1/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.JsonUtils.parseStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeStringList
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Stored payment method model for instant/unknown stored payment methods.
 *
 * This is the fallback type used when a stored payment method type is not explicitly supported.
 * It contains only the base fields.
 */
@Parcelize
data class StoredInstantPaymentMethod(
    override val type: String,
    override val name: String,
    override val id: String,
    override val supportedShopperInteractions: List<String>,
) : StoredPaymentMethod() {

    companion object {
        @JvmField
        val SERIALIZER: Serializer<StoredInstantPaymentMethod> =
            object : Serializer<StoredInstantPaymentMethod> {
                override fun serialize(modelObject: StoredInstantPaymentMethod): JSONObject {
                    return try {
                        JSONObject().apply {
                            put(TYPE, modelObject.type)
                            put(NAME, modelObject.name)
                            put(ID, modelObject.id)
                            put(
                                SUPPORTED_SHOPPER_INTERACTIONS,
                                serializeStringList(modelObject.supportedShopperInteractions),
                            )
                        }
                    } catch (e: JSONException) {
                        throw ModelSerializationException(StoredInstantPaymentMethod::class.java, e)
                    }
                }

                override fun deserialize(jsonObject: JSONObject): StoredInstantPaymentMethod {
                    return try {
                        StoredInstantPaymentMethod(
                            type = jsonObject.getString(TYPE),
                            name = jsonObject.getString(NAME),
                            id = jsonObject.getString(ID),
                            supportedShopperInteractions = parseStringList(
                                jsonObject.getJSONArray(SUPPORTED_SHOPPER_INTERACTIONS),
                            ),
                        )
                    } catch (e: JSONException) {
                        throw ModelSerializationException(StoredInstantPaymentMethod::class.java, e)
                    }
                }
            }
    }
}
