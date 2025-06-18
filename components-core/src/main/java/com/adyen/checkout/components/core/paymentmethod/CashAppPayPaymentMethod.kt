package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class CashAppPayPaymentMethod(
    override var type: String?,
    override var checkoutAttemptId: String?,
    var grantId: String? = null,
    var onFileGrantId: String? = null,
    var customerId: String? = null,
    var cashtag: String? = null,
    var storedPaymentMethodId: String? = null,
) : PaymentMethodDetails() {

    companion object {

        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.CASH_APP_PAY

        private const val GRANT_ID = "grantId"
        private const val ON_FILE_GRANT_ID = "onFileGrantId"
        private const val CUSTOMER_ID = "customerId"
        private const val CASH_TAG = "cashtag"
        private const val STORED_PAYMENT_METHOD_ID = "storedPaymentMethodId"

        val SERIALIZER: Serializer<CashAppPayPaymentMethod> = object : Serializer<CashAppPayPaymentMethod> {
            override fun serialize(modelObject: CashAppPayPaymentMethod): JSONObject = JSONObject().apply {
                try {
                    putOpt(TYPE, modelObject.type)
                    putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
                    putOpt(GRANT_ID, modelObject.grantId)
                    putOpt(ON_FILE_GRANT_ID, modelObject.onFileGrantId)
                    putOpt(CUSTOMER_ID, modelObject.customerId)
                    putOpt(CASH_TAG, modelObject.cashtag)
                    putOpt(STORED_PAYMENT_METHOD_ID, modelObject.storedPaymentMethodId)
                } catch (e: JSONException) {
                    throw ModelSerializationException(CashAppPayPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): CashAppPayPaymentMethod = CashAppPayPaymentMethod(
                type = jsonObject.getStringOrNull(TYPE),
                checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                grantId = jsonObject.getStringOrNull(GRANT_ID),
                onFileGrantId = jsonObject.getStringOrNull(ON_FILE_GRANT_ID),
                customerId = jsonObject.getStringOrNull(CUSTOMER_ID),
                cashtag = jsonObject.getStringOrNull(CASH_TAG),
                storedPaymentMethodId = jsonObject.getStringOrNull(STORED_PAYMENT_METHOD_ID),
            )
        }
    }
}
