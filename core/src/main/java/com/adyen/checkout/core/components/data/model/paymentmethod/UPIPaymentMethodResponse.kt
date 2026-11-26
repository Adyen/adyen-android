/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 25/2/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.internal.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.common.internal.model.ModelUtils.serializeOptList
import com.adyen.checkout.core.components.data.model.AppData
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Payment method model for UPI.
 */
@Parcelize
data class UPIPaymentMethodResponse(
    override val type: String,
    override val name: String,
    val apps: List<AppData>?,
) : RegularPaymentMethodResponse() {

    companion object {
        private const val APPS = "apps"

        @JvmField
        val SERIALIZER: Serializer<UPIPaymentMethodResponse> = object : Serializer<UPIPaymentMethodResponse> {
            override fun serialize(modelObject: UPIPaymentMethodResponse): JSONObject {
                return JSONObject().apply {
                    put(TYPE, modelObject.type)
                    put(NAME, modelObject.name)
                    putOpt(APPS, serializeOptList(modelObject.apps, AppData.SERIALIZER))
                }
            }

            override fun deserialize(jsonObject: JSONObject): UPIPaymentMethodResponse {
                return UPIPaymentMethodResponse(
                    type = jsonObject.getString(TYPE),
                    name = jsonObject.getString(NAME),
                    apps = deserializeOptList(
                        jsonObject.optJSONArray(APPS),
                        AppData.SERIALIZER,
                    ),
                )
            }
        }
    }
}
