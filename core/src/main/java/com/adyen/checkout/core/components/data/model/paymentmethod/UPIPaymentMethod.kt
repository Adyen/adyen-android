/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 25/2/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.common.internal.model.ModelUtils.serializeOptList
import com.adyen.checkout.core.components.data.model.AppData
import com.adyen.checkout.core.components.data.model.Issuer
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Payment method model for UPI.
 */
@Parcelize
data class UPIPaymentMethod(
    override val type: String,
    override val name: String,
    val apps: List<AppData>? = null,
) : PaymentMethod() {

    companion object {
        private const val APPS = "apps"

        @JvmField
        val SERIALIZER: Serializer<UPIPaymentMethod> = object : Serializer<UPIPaymentMethod> {
            override fun serialize(modelObject: UPIPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        put(TYPE, modelObject.type)
                        put(NAME, modelObject.name)
                        putOpt(APPS, serializeOptList(modelObject.apps, AppData.SERIALIZER))
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(UPIPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): UPIPaymentMethod {
                return try {
                    UPIPaymentMethod(
                        type = jsonObject.getString(TYPE),
                        name = jsonObject.getString(NAME),
                        apps = deserializeOptList(
                            jsonObject.optJSONArray(APPS),
                            AppData.SERIALIZER,
                        ),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(UPIPaymentMethod::class.java, e)
                }
            }
        }
    }
}
