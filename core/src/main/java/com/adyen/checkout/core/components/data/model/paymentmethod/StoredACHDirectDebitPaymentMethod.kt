package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.JsonUtils.parseOptStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeOptStringList
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class StoredACHDirectDebitPaymentMethod(
    override val type: String,
    override val name: String,
    override val id: String,
    override val supportedShopperInteractions: List<String>,
    val bankAccountNumber: String,
) : StoredPaymentMethod() {

    companion object {
        private const val BANK_ACCOUNT_NUMBER = "bankAccountNumber"

        @JvmField
        val SERIALIZER: Serializer<StoredACHDirectDebitPaymentMethod> =
            object : Serializer<StoredACHDirectDebitPaymentMethod> {
                override fun serialize(modelObject: StoredACHDirectDebitPaymentMethod): JSONObject {
                    return try {
                        JSONObject().apply {
                            put(TYPE, modelObject.type)
                            put(NAME, modelObject.name)
                            put(ID, modelObject.id)
                            putOpt(
                                SUPPORTED_SHOPPER_INTERACTIONS,
                                serializeOptStringList(modelObject.supportedShopperInteractions),
                            )
                            put(BANK_ACCOUNT_NUMBER, modelObject.bankAccountNumber)
                        }
                    } catch (e: JSONException) {
                        throw ModelSerializationException(StoredACHDirectDebitPaymentMethod::class.java, e)
                    }
                }

                override fun deserialize(jsonObject: JSONObject): StoredACHDirectDebitPaymentMethod {
                    return try {
                        StoredACHDirectDebitPaymentMethod(
                            type = jsonObject.getString(TYPE),
                            name = jsonObject.getString(NAME),
                            id = jsonObject.getString(ID),
                            supportedShopperInteractions = parseOptStringList(
                                jsonObject.optJSONArray(SUPPORTED_SHOPPER_INTERACTIONS),
                            ) ?: emptyList(),
                            bankAccountNumber = jsonObject.getString(BANK_ACCOUNT_NUMBER),
                        )
                    } catch (e: JSONException) {
                        throw ModelSerializationException(StoredACHDirectDebitPaymentMethod::class.java, e)
                    }
                }
            }
    }
}
