package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONObject


@Parcelize
data class CashAppPayPaymentMethod(
    override var type: String? = null,
    var grantId: String? = null,
) : PaymentMethodDetails() {

    companion object {

        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.CASH_APP_PAY

        private const val GRANT_ID = "grantId"

        val SERIALIZER: Serializer<CashAppPayPaymentMethod> = object : Serializer<CashAppPayPaymentMethod> {
            override fun serialize(modelObject: CashAppPayPaymentMethod): JSONObject = JSONObject().apply {
                putOpt(TYPE, modelObject.type)
                putOpt(GRANT_ID, modelObject.grantId)
            }

            override fun deserialize(jsonObject: JSONObject): CashAppPayPaymentMethod = CashAppPayPaymentMethod(
                type = jsonObject.getStringOrNull(TYPE),
                grantId = jsonObject.getStringOrNull(GRANT_ID),
            )
        }
    }
}
