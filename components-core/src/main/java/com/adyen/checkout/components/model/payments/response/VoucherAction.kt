/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/6/2019.
 */
package com.adyen.checkout.components.model.payments.response

import android.os.Parcel
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.util.ActionTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.model.ModelUtils.serializeOpt
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class VoucherAction(
    override var type: String? = null,
    override var paymentData: String? = null,
    override var paymentMethodType: String? = null,
    var surcharge: Amount? = null,
    var initialAmount: Amount? = null,
    var totalAmount: Amount? = null,
    var issuer: String? = null,
    var expiresAt: String? = null,
    var reference: String? = null,
    var alternativeReference: String? = null,
    var merchantName: String? = null,
    var url: String? = null,
) : Action() {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        const val ACTION_TYPE = ActionTypes.VOUCHER
        private const val SURCHARGE = "surcharge"
        private const val INITIAL_AMOUNT = "initialAmount"
        private const val TOTAL_AMOUNT = "totalAmount"
        private const val ISSUER = "issuer"
        private const val EXPIRES_AT = "expiresAt"
        private const val REFERENCE = "reference"
        private const val ALTERNATIVE_REFERENCE = "alternativeReference"
        private const val MERCHANT_NAME = "merchantName"
        private const val URL = "url"

        @JvmField
        val CREATOR = Creator(VoucherAction::class.java)

        @JvmField
        val SERIALIZER: Serializer<VoucherAction> = object : Serializer<VoucherAction> {
            override fun serialize(modelObject: VoucherAction): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(PAYMENT_DATA, modelObject.paymentData)
                        putOpt(PAYMENT_METHOD_TYPE, modelObject.paymentMethodType)
                        putOpt(SURCHARGE, serializeOpt(modelObject.surcharge, Amount.SERIALIZER))
                        putOpt(INITIAL_AMOUNT, serializeOpt(modelObject.initialAmount, Amount.SERIALIZER))
                        putOpt(TOTAL_AMOUNT, serializeOpt(modelObject.totalAmount, Amount.SERIALIZER))
                        putOpt(ISSUER, modelObject.issuer)
                        putOpt(EXPIRES_AT, modelObject.expiresAt)
                        putOpt(REFERENCE, modelObject.reference)
                        putOpt(ALTERNATIVE_REFERENCE, modelObject.alternativeReference)
                        putOpt(MERCHANT_NAME, modelObject.merchantName)
                        putOpt(URL, modelObject.url)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(VoucherAction::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): VoucherAction {
                return VoucherAction(
                    type = jsonObject.getStringOrNull(TYPE),
                    paymentData = jsonObject.getStringOrNull(PAYMENT_DATA),
                    paymentMethodType = jsonObject.getStringOrNull(PAYMENT_METHOD_TYPE),
                    surcharge = deserializeOpt(jsonObject.optJSONObject(SURCHARGE), Amount.SERIALIZER),
                    initialAmount = deserializeOpt(jsonObject.optJSONObject(INITIAL_AMOUNT), Amount.SERIALIZER),
                    totalAmount = deserializeOpt(jsonObject.optJSONObject(TOTAL_AMOUNT), Amount.SERIALIZER),
                    issuer = jsonObject.getStringOrNull(ISSUER),
                    expiresAt = jsonObject.getStringOrNull(EXPIRES_AT),
                    reference = jsonObject.getStringOrNull(REFERENCE),
                    alternativeReference = jsonObject.getStringOrNull(ALTERNATIVE_REFERENCE),
                    merchantName = jsonObject.getStringOrNull(MERCHANT_NAME),
                    url = jsonObject.getStringOrNull(URL),
                )
            }
        }
    }
}
