package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.JsonUtils.parseOptStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeOptStringList
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
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
    val holderName: String? = null,
) : StoredPaymentMethod() {

    companion object {
        private const val BRAND = "brand"
        private const val LAST_FOUR = "lastFour"
        private const val EXPIRY_MONTH = "expiryMonth"
        private const val EXPIRY_YEAR = "expiryYear"
        private const val HOLDER_NAME = "holderName"

        @JvmField
        val SERIALIZER: Serializer<StoredBCMCPaymentMethod> = object : Serializer<StoredBCMCPaymentMethod> {
            override fun serialize(modelObject: StoredBCMCPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        put(TYPE, modelObject.type)
                        put(NAME, modelObject.name)
                        put(ID, modelObject.id)
                        putOpt(
                            SUPPORTED_SHOPPER_INTERACTIONS,
                            serializeOptStringList(modelObject.supportedShopperInteractions),
                        )
                        put(BRAND, modelObject.brand)
                        put(LAST_FOUR, modelObject.lastFour)
                        put(EXPIRY_MONTH, modelObject.expiryMonth)
                        put(EXPIRY_YEAR, modelObject.expiryYear)
                        putOpt(HOLDER_NAME, modelObject.holderName)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(StoredBCMCPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): StoredBCMCPaymentMethod {
                return try {
                    StoredBCMCPaymentMethod(
                        type = jsonObject.getString(TYPE),
                        name = jsonObject.getString(NAME),
                        id = jsonObject.getString(ID),
                        supportedShopperInteractions = parseOptStringList(
                            jsonObject.optJSONArray(SUPPORTED_SHOPPER_INTERACTIONS),
                        ) ?: emptyList(),
                        brand = jsonObject.getString(BRAND),
                        lastFour = jsonObject.getString(LAST_FOUR),
                        expiryMonth = jsonObject.getString(EXPIRY_MONTH),
                        expiryYear = jsonObject.getString(EXPIRY_YEAR),
                        holderName = jsonObject.getStringOrNull(HOLDER_NAME),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(StoredBCMCPaymentMethod::class.java, e)
                }
            }
        }
    }
}
