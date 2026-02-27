/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 25/2/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.JsonUtils.parseOptStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeOptStringList
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Payment method model for a stored Twint payment method.
 */
@Parcelize
data class StoredTwintPaymentMethod(
    override val type: String,
    override val name: String,
    override val id: String,
    override val supportedShopperInteractions: List<String>,
) : StoredPaymentMethod() {

    companion object {
        @JvmField
        val SERIALIZER: Serializer<StoredTwintPaymentMethod> = object : Serializer<StoredTwintPaymentMethod> {
            override fun serialize(modelObject: StoredTwintPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        put(TYPE, modelObject.type)
                        put(NAME, modelObject.name)
                        put(ID, modelObject.id)
                        putOpt(
                            SUPPORTED_SHOPPER_INTERACTIONS,
                            serializeOptStringList(modelObject.supportedShopperInteractions),
                        )
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(StoredTwintPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): StoredTwintPaymentMethod {
                return try {
                    StoredTwintPaymentMethod(
                        type = jsonObject.getString(TYPE),
                        name = jsonObject.getString(NAME),
                        id = jsonObject.getString(ID),
                        supportedShopperInteractions = parseOptStringList(
                            jsonObject.optJSONArray(SUPPORTED_SHOPPER_INTERACTIONS),
                        ) ?: emptyList(),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(StoredTwintPaymentMethod::class.java, e)
                }
            }
        }
    }
}
