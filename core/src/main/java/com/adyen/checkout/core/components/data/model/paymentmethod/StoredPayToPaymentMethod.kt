package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.JsonUtils.parseOptStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeOptStringList
import kotlinx.parcelize.Parcelize
import org.json.JSONException
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
                    return try {
                        JSONObject().apply {
                            put(TYPE, modelObject.type)
                            put(NAME, modelObject.name)
                            put(ID, modelObject.id)
                            putOpt(
                                SUPPORTED_SHOPPER_INTERACTIONS,
                                serializeOptStringList(modelObject.supportedShopperInteractions),
                            )
                            put(LABEL, modelObject.label)
                        }
                    } catch (e: JSONException) {
                        throw ModelSerializationException(StoredPayToPaymentMethod::class.java, e)
                    }
                }

                override fun deserialize(jsonObject: JSONObject): StoredPayToPaymentMethod {
                    return try {
                        StoredPayToPaymentMethod(
                            type = jsonObject.getString(TYPE),
                            name = jsonObject.getString(NAME),
                            id = jsonObject.getString(ID),
                            supportedShopperInteractions = parseOptStringList(
                                jsonObject.optJSONArray(SUPPORTED_SHOPPER_INTERACTIONS),
                            ) ?: emptyList(),
                            label = jsonObject.getString(LABEL),
                        )
                    } catch (e: JSONException) {
                        throw ModelSerializationException(StoredPayToPaymentMethod::class.java, e)
                    }
                }
            }
    }
}
