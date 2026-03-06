/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/3/2026.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.JsonUtils.parseStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeStringList
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * A [StoredPaymentMethod] representing a PayPal stored payment method.
 */
@Parcelize
data class StoredPayPalPaymentMethod(
    override val type: String,
    override val name: String,
    override val id: String,
    override val supportedShopperInteractions: List<String>,
    val shopperEmail: String,
) : StoredPaymentMethod() {

    companion object {
        private const val SHOPPER_EMAIL = "shopperEmail"

        @JvmField
        val SERIALIZER: Serializer<StoredPayPalPaymentMethod> = object : Serializer<StoredPayPalPaymentMethod> {
            override fun serialize(modelObject: StoredPayPalPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        put(TYPE, modelObject.type)
                        put(NAME, modelObject.name)
                        put(ID, modelObject.id)
                        put(
                            SUPPORTED_SHOPPER_INTERACTIONS,
                            serializeStringList(modelObject.supportedShopperInteractions),
                        )
                        put(SHOPPER_EMAIL, modelObject.shopperEmail)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(StoredPayPalPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): StoredPayPalPaymentMethod {
                return try {
                    StoredPayPalPaymentMethod(
                        type = jsonObject.getString(TYPE),
                        name = jsonObject.getString(NAME),
                        id = jsonObject.getString(ID),
                        supportedShopperInteractions = parseStringList(
                            jsonObject.getJSONArray(SUPPORTED_SHOPPER_INTERACTIONS),
                        ),
                        shopperEmail = jsonObject.getString(SHOPPER_EMAIL),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(StoredPayPalPaymentMethod::class.java, e)
                }
            }
        }
    }
}
