package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.internal.model.JsonUtils.parseStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeStringList
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class StoredCashAppPayPaymentMethod(
    override val type: String,
    override val name: String,
    override val id: String,
    override val supportedShopperInteractions: List<String>,
    val cashtag: String,
) : StoredPaymentMethod() {

    companion object {
        private const val CASHTAG = "cashtag"

        @JvmField
        val SERIALIZER: Serializer<StoredCashAppPayPaymentMethod> =
            object : Serializer<StoredCashAppPayPaymentMethod> {
                override fun serialize(modelObject: StoredCashAppPayPaymentMethod): JSONObject {
                    return JSONObject().apply {
                        put(TYPE, modelObject.type)
                        put(NAME, modelObject.name)
                        put(ID, modelObject.id)
                        put(
                            SUPPORTED_SHOPPER_INTERACTIONS,
                            serializeStringList(modelObject.supportedShopperInteractions),
                        )
                        put(CASHTAG, modelObject.cashtag)
                    }
                }

                override fun deserialize(jsonObject: JSONObject): StoredCashAppPayPaymentMethod {
                    return StoredCashAppPayPaymentMethod(
                        type = jsonObject.getString(TYPE),
                        name = jsonObject.getString(NAME),
                        id = jsonObject.getString(ID),
                        supportedShopperInteractions = parseStringList(
                            jsonObject.getJSONArray(SUPPORTED_SHOPPER_INTERACTIONS),
                        ),
                        cashtag = jsonObject.getString(CASHTAG),
                    )
                }
            }
    }
}
