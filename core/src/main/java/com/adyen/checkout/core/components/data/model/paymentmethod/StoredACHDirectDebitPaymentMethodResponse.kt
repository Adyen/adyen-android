package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.internal.model.JsonUtils.parseStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeStringList
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class StoredACHDirectDebitPaymentMethodResponse(
    override val type: String,
    override val name: String,
    override val id: String,
    override val supportedShopperInteractions: List<String>,
    val bankAccountNumber: String,
) : StoredPaymentMethodResponse() {

    companion object {
        private const val BANK_ACCOUNT_NUMBER = "bankAccountNumber"

        @JvmField
        val SERIALIZER: Serializer<StoredACHDirectDebitPaymentMethodResponse> =
            object : Serializer<StoredACHDirectDebitPaymentMethodResponse> {
                override fun serialize(modelObject: StoredACHDirectDebitPaymentMethodResponse): JSONObject {
                    return JSONObject().apply {
                        put(TYPE, modelObject.type)
                        put(NAME, modelObject.name)
                        put(ID, modelObject.id)
                        put(
                            SUPPORTED_SHOPPER_INTERACTIONS,
                            serializeStringList(modelObject.supportedShopperInteractions),
                        )
                        put(BANK_ACCOUNT_NUMBER, modelObject.bankAccountNumber)
                    }
                }

                override fun deserialize(jsonObject: JSONObject): StoredACHDirectDebitPaymentMethodResponse {
                    return StoredACHDirectDebitPaymentMethodResponse(
                        type = jsonObject.getString(TYPE),
                        name = jsonObject.getString(NAME),
                        id = jsonObject.getString(ID),
                        supportedShopperInteractions = parseStringList(
                            jsonObject.getJSONArray(SUPPORTED_SHOPPER_INTERACTIONS),
                        ),
                        bankAccountNumber = jsonObject.getString(BANK_ACCOUNT_NUMBER),
                    )
                }
            }
    }
}
