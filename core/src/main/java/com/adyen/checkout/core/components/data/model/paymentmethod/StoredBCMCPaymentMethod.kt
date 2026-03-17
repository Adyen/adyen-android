package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.internal.model.JsonUtils.parseStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeStringList
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class StoredBCMCPaymentMethod(
    override val type: String,
    override val name: String,
    override val id: String,
    override val supportedShopperInteractions: List<String>,
    val brand: String,
    val lastFour: String,
    val expiryMonth: String,
    val expiryYear: String,
    val holderName: String?,
) : StoredPaymentMethod() {

    companion object {
        private const val BRAND = "brand"
        private const val LAST_FOUR = "lastFour"
        private const val EXPIRY_MONTH = "expiryMonth"
        private const val EXPIRY_YEAR = "expiryYear"
        private const val HOLDER_NAME = "holderName"

        @JvmField
        val SERIALIZER: Serializer<StoredBCMCPaymentMethod> =
            object : Serializer<StoredBCMCPaymentMethod> {
                override fun serialize(modelObject: StoredBCMCPaymentMethod): JSONObject {
                    return JSONObject().apply {
                        put(TYPE, modelObject.type)
                        put(NAME, modelObject.name)
                        put(ID, modelObject.id)
                        put(
                            SUPPORTED_SHOPPER_INTERACTIONS,
                            serializeStringList(modelObject.supportedShopperInteractions),
                        )
                        put(BRAND, modelObject.brand)
                        put(LAST_FOUR, modelObject.lastFour)
                        put(EXPIRY_MONTH, modelObject.expiryMonth)
                        put(EXPIRY_YEAR, modelObject.expiryYear)
                        putOpt(HOLDER_NAME, modelObject.holderName)
                    }
                }

                override fun deserialize(jsonObject: JSONObject): StoredBCMCPaymentMethod {
                    return StoredBCMCPaymentMethod(
                        type = jsonObject.getString(TYPE),
                        name = jsonObject.getString(NAME),
                        id = jsonObject.getString(ID),
                        supportedShopperInteractions = parseStringList(
                            jsonObject.getJSONArray(SUPPORTED_SHOPPER_INTERACTIONS),
                        ),
                        brand = jsonObject.getString(BRAND),
                        lastFour = jsonObject.getString(LAST_FOUR),
                        expiryMonth = jsonObject.getString(EXPIRY_MONTH),
                        expiryYear = jsonObject.getString(EXPIRY_YEAR),
                        holderName = jsonObject.getStringOrNull(HOLDER_NAME),
                    )
                }
            }
    }
}
