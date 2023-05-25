/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 6/3/2023.
 */

package com.adyen.checkout.components.model.payments.request

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class CashAppPayPaymentMethod(
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

        @JvmField
        val CREATOR: Parcelable.Creator<CashAppPayPaymentMethod> = Creator(CashAppPayPaymentMethod::class.java)

        @JvmField
        val SERIALIZER: Serializer<CashAppPayPaymentMethod> = object : Serializer<CashAppPayPaymentMethod> {

            override fun serialize(modelObject: CashAppPayPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(GRANT_ID, modelObject.grantId)
                        putOpt(ON_FILE_GRANT_ID, modelObject.onFileGrantId)
                        putOpt(CUSTOMER_ID, modelObject.customerId)
                        putOpt(CASH_TAG, modelObject.cashtag)
                        putOpt(STORED_PAYMENT_METHOD_ID, modelObject.storedPaymentMethodId)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(CashAppPayPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): CashAppPayPaymentMethod {
                return CashAppPayPaymentMethod().apply {
                    type = jsonObject.getStringOrNull(TYPE)
                    grantId = jsonObject.getStringOrNull(GRANT_ID)
                    onFileGrantId = jsonObject.getStringOrNull(ON_FILE_GRANT_ID)
                    customerId = jsonObject.getStringOrNull(CUSTOMER_ID)
                    cashtag = jsonObject.getStringOrNull(CASH_TAG)
                    storedPaymentMethodId = jsonObject.getStringOrNull(STORED_PAYMENT_METHOD_ID)
                }
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        JsonUtils.writeToParcel(parcel, SERIALIZER.serialize(this))
    }
}
