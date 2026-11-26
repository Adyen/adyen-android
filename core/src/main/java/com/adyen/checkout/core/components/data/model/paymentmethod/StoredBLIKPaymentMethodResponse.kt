/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 25/2/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.internal.model.JsonUtils.parseStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeStringList
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Payment method model for a stored BLIK payment method.
 */
@Parcelize
data class StoredBLIKPaymentMethodResponse(
    override val type: String,
    override val name: String,
    override val id: String,
    override val supportedShopperInteractions: List<String>,
) : StoredPaymentMethodResponse() {

    companion object {
        @JvmField
        val SERIALIZER: Serializer<StoredBLIKPaymentMethodResponse> =
            object : Serializer<StoredBLIKPaymentMethodResponse> {
                override fun serialize(modelObject: StoredBLIKPaymentMethodResponse): JSONObject {
                    return JSONObject().apply {
                        put(TYPE, modelObject.type)
                        put(NAME, modelObject.name)
                        put(ID, modelObject.id)
                        put(
                            SUPPORTED_SHOPPER_INTERACTIONS,
                            serializeStringList(modelObject.supportedShopperInteractions),
                        )
                    }
                }

                override fun deserialize(jsonObject: JSONObject): StoredBLIKPaymentMethodResponse {
                    return StoredBLIKPaymentMethodResponse(
                        type = jsonObject.getString(TYPE),
                        name = jsonObject.getString(NAME),
                        id = jsonObject.getString(ID),
                        supportedShopperInteractions = parseStringList(
                            jsonObject.getJSONArray(SUPPORTED_SHOPPER_INTERACTIONS),
                        ),
                    )
                }
            }
    }
}
