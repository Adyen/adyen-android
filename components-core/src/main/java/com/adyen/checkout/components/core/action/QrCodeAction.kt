/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/6/2019.
 */
package com.adyen.checkout.components.core.action

import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class QrCodeAction(
    override var type: String? = null,
    override var paymentData: String? = null,
    override var paymentMethodType: String? = null,
    var qrCodeData: String? = null,
    var url: String? = null,
) : Action() {

    companion object {
        const val ACTION_TYPE = ActionTypes.QR_CODE
        private const val QR_CODE_DATA = "qrCodeData"
        private const val URL = "url"

        @JvmField
        val SERIALIZER: Serializer<QrCodeAction> = object : Serializer<QrCodeAction> {
            override fun serialize(modelObject: QrCodeAction): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(PAYMENT_DATA, modelObject.paymentData)
                        putOpt(PAYMENT_METHOD_TYPE, modelObject.paymentMethodType)
                        putOpt(QR_CODE_DATA, modelObject.qrCodeData)
                        putOpt(URL, modelObject.url)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(QrCodeAction::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): QrCodeAction {
                return QrCodeAction(
                    type = jsonObject.getStringOrNull(TYPE),
                    paymentData = jsonObject.getStringOrNull(PAYMENT_DATA),
                    paymentMethodType = jsonObject.getStringOrNull(PAYMENT_METHOD_TYPE),
                    qrCodeData = jsonObject.getStringOrNull(QR_CODE_DATA),
                    url = jsonObject.getStringOrNull(URL),
                )
            }
        }
    }
}
