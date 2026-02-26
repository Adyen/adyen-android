package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.JsonUtils.parseOptStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeOptStringList
import kotlinx.parcelize.Parcelize
import org.json.JSONException
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
                    return try {
                        JSONObject().apply {
                            put(TYPE, modelObject.type)
                            put(NAME, modelObject.name)
                            put(ID, modelObject.id)
                            putOpt(
                                SUPPORTED_SHOPPER_INTERACTIONS,
                                serializeOptStringList(modelObject.supportedShopperInteractions),
                            )
                            put(CASHTAG, modelObject.cashtag)
                        }
                    } catch (e: JSONException) {
                        throw ModelSerializationException(StoredCashAppPayPaymentMethod::class.java, e)
                    }
                }

                override fun deserialize(jsonObject: JSONObject): StoredCashAppPayPaymentMethod {
                    return try {
                        StoredCashAppPayPaymentMethod(
                            type = jsonObject.getString(TYPE),
                            name = jsonObject.getString(NAME),
                            id = jsonObject.getString(ID),
                            supportedShopperInteractions = parseOptStringList(
                                jsonObject.optJSONArray(SUPPORTED_SHOPPER_INTERACTIONS),
                            ) ?: emptyList(),
                            cashtag = jsonObject.getString(CASHTAG),
                        )
                    } catch (e: JSONException) {
                        throw ModelSerializationException(StoredCashAppPayPaymentMethod::class.java, e)
                    }
                }
            }
    }
}
