package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.internal.model.JsonUtils.parseStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeStringList
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class StoredPayToPaymentMethod(
    override val type: String,
    override val name: String,
    override val id: String,
    override val supportedShopperInteractions: List<String>,
    val label: String,
) : StoredPaymentMethod() {

    companion object {
        private const val LABEL = "label"

        @JvmField
        val SERIALIZER: Serializer<StoredPayToPaymentMethod> =
            object : Serializer<StoredPayToPaymentMethod> {
                override fun serialize(modelObject: StoredPayToPaymentMethod): JSONObject {
                    return JSONObject().apply {
                        put(TYPE, modelObject.type)
                        put(NAME, modelObject.name)
                        put(ID, modelObject.id)
                        put(
                            SUPPORTED_SHOPPER_INTERACTIONS,
                            serializeStringList(modelObject.supportedShopperInteractions),
                        )
                        put(LABEL, modelObject.label)
                    }
                }

                override fun deserialize(jsonObject: JSONObject): StoredPayToPaymentMethod {
                    return StoredPayToPaymentMethod(
                        type = jsonObject.getString(TYPE),
                        name = jsonObject.getString(NAME),
                        id = jsonObject.getString(ID),
                        supportedShopperInteractions = parseStringList(
                            jsonObject.getJSONArray(SUPPORTED_SHOPPER_INTERACTIONS),
                        ),
                        label = jsonObject.getString(LABEL),
                    )
                }
            }
    }
}
