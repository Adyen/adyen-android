/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/5/2020.
 */
package com.adyen.checkout.components.model.payments.response

import android.os.Parcel
import com.adyen.checkout.components.util.ActionTypes
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.model.ModelUtils.serializeOpt
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class SdkAction<SdkDataT : SdkData>(
    override var type: String? = null,
    override var paymentData: String? = null,
    override var paymentMethodType: String? = null,
    var sdkData: SdkDataT? = null,
) : Action() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        const val ACTION_TYPE = ActionTypes.SDK
        private const val SDK_DATA = "sdkData"

        @JvmField
        val CREATOR = Creator(SdkAction::class.java)

        @JvmField
        val SERIALIZER: Serializer<SdkAction<*>> = object : Serializer<SdkAction<*>> {
            override fun serialize(modelObject: SdkAction<*>): JSONObject {
                val sdkDataSerializer = getSdkDataSerializer(modelObject.paymentMethodType)
                return try {
                    JSONObject().apply {
                        // Get parameters from parent class
                        putOpt(TYPE, modelObject.type)
                        putOpt(PAYMENT_DATA, modelObject.paymentData)
                        putOpt(PAYMENT_METHOD_TYPE, modelObject.paymentMethodType)
                        putOpt(SDK_DATA, serializeOpt(modelObject.sdkData, sdkDataSerializer))
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(SdkAction::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): SdkAction<SdkData> {
                val paymentMethodType = jsonObject.getStringOrNull(PAYMENT_METHOD_TYPE)
                val sdkDataSerializer = getSdkDataSerializer(paymentMethodType)

                return SdkAction(
                    type = jsonObject.getStringOrNull(TYPE),
                    paymentData = jsonObject.getStringOrNull(PAYMENT_DATA),
                    paymentMethodType = paymentMethodType,
                    sdkData = deserializeOpt(jsonObject.optJSONObject(SDK_DATA), sdkDataSerializer),
                )
            }

            private fun getSdkDataSerializer(paymentMethodType: String?): Serializer<SdkData> {
                if (paymentMethodType.isNullOrEmpty()) {
                    throw CheckoutException("SdkAction cannot be parsed with null paymentMethodType.")
                }
                @Suppress("UNCHECKED_CAST")
                return when (paymentMethodType) {
                    PaymentMethodTypes.WECHAT_PAY_SDK -> WeChatPaySdkData.SERIALIZER as Serializer<SdkData>
                    else -> throw CheckoutException("sdkData not found for type paymentMethodType - $paymentMethodType")
                }
            }
        }
    }
}
