/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/1/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.JsonUtils.parseStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeStringList
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Stored payment method model for unsupported stored payment methods.
 *
 * This is used when a stored payment method type is explicitly not supported by the SDK.
 * It contains only the base fields.
 */
@Parcelize
data class StoredUnsupportedPaymentMethod(
    override val type: String,
    override val name: String,
    override val id: String,
    override val supportedShopperInteractions: List<String>,
) : StoredPaymentMethod() {

    companion object {
        @JvmField
        val SERIALIZER: Serializer<StoredUnsupportedPaymentMethod> =
            object : Serializer<StoredUnsupportedPaymentMethod> {
                override fun serialize(modelObject: StoredUnsupportedPaymentMethod): JSONObject {
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
                        throw ModelSerializationException(StoredUnsupportedPaymentMethod::class.java, e)
                    }
                }

                override fun deserialize(jsonObject: JSONObject): StoredUnsupportedPaymentMethod {
                    return try {
                        StoredUnsupportedPaymentMethod(
                            type = jsonObject.getString(TYPE),
                            name = jsonObject.getString(NAME),
                            id = jsonObject.getString(ID),
                            supportedShopperInteractions = parseStringList(
                                jsonObject.getJSONArray(SUPPORTED_SHOPPER_INTERACTIONS),
                            ),
                        )
                    } catch (e: JSONException) {
                        throw ModelSerializationException(StoredUnsupportedPaymentMethod::class.java, e)
                    }
                }
            }
    }
}
