/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/6/2019.
 */
package com.adyen.checkout.core.action.data

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class RedirectAction(
    override val type: String? = null,
    override val paymentData: String? = null,
    override val paymentMethodType: String? = null,
    val method: String? = null,
    val url: String? = null,
    val nativeRedirectData: String? = null,
) : Action() {

    companion object {
        const val ACTION_TYPE = ActionTypes.REDIRECT
        private const val METHOD = "method"
        private const val URL = "url"
        private const val NATIVE_REDIRECT_DATA = "nativeRedirectData"

        @JvmField
        val SERIALIZER: Serializer<RedirectAction> = object : Serializer<RedirectAction> {
            override fun serialize(modelObject: RedirectAction): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(PAYMENT_DATA, modelObject.paymentData)
                        putOpt(PAYMENT_METHOD_TYPE, modelObject.paymentMethodType)
                        putOpt(METHOD, modelObject.method)
                        putOpt(URL, modelObject.url)
                        putOpt(NATIVE_REDIRECT_DATA, modelObject.nativeRedirectData)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(RedirectAction::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): RedirectAction {
                return RedirectAction(
                    type = jsonObject.getStringOrNull(TYPE),
                    paymentData = jsonObject.getStringOrNull(PAYMENT_DATA),
                    paymentMethodType = jsonObject.getStringOrNull(PAYMENT_METHOD_TYPE),
                    method = jsonObject.getStringOrNull(METHOD),
                    url = jsonObject.getStringOrNull(URL),
                    nativeRedirectData = jsonObject.getStringOrNull(NATIVE_REDIRECT_DATA),
                )
            }
        }
    }
}
