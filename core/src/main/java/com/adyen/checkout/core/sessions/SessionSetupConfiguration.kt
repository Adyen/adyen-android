/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/5/2025.
 */

package com.adyen.checkout.core.sessions

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getBooleanOrNull
import com.adyen.checkout.core.exception.ModelSerializationException
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class SessionSetupConfiguration(
    val enableStoreDetails: Boolean? = null,
    val showInstallmentAmount: Boolean = false,
    // TODO - Installments
//    val installmentOptions: Map<String, SessionSetupInstallmentOptions?>? = null,
    val showRemovePaymentMethodButton: Boolean? = null,
) : ModelObject() {

    companion object {
        private const val ENABLE_STORE_DETAILS = "enableStoreDetails"
        private const val SHOW_INSTALLMENT_AMOUNT = "showInstallmentAmount"

        // TODO - Installments
//        private const val INSTALLMENT_OPTIONS = "installmentOptions"

        private const val SHOW_REMOVE_PAYMENT_METHOD_BUTTON = "showRemovePaymentMethodButton"

        @JvmField
        val SERIALIZER: Serializer<SessionSetupConfiguration> = object : Serializer<SessionSetupConfiguration> {
            override fun serialize(modelObject: SessionSetupConfiguration): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(ENABLE_STORE_DETAILS, modelObject.enableStoreDetails)
                        putOpt(SHOW_INSTALLMENT_AMOUNT, modelObject.showInstallmentAmount)
//                        putOpt(
//                            INSTALLMENT_OPTIONS,
//                            modelObject.installmentOptions?.let { JSONObject(it) },
//                        )
                        putOpt(SHOW_REMOVE_PAYMENT_METHOD_BUTTON, modelObject.showRemovePaymentMethodButton)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionSetupConfiguration::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): SessionSetupConfiguration {
                return try {
                    SessionSetupConfiguration(
                        enableStoreDetails = jsonObject.getBooleanOrNull(ENABLE_STORE_DETAILS),
                        showInstallmentAmount = jsonObject.getBooleanOrNull(SHOW_INSTALLMENT_AMOUNT) ?: false,
//                        installmentOptions = jsonObject.optJSONObject(INSTALLMENT_OPTIONS)
//                            ?.jsonToMap(SessionSetupInstallmentOptions.SERIALIZER),
                        showRemovePaymentMethodButton = jsonObject.getBooleanOrNull(
                            SHOW_REMOVE_PAYMENT_METHOD_BUTTON
                        ),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionSetupConfiguration::class.java, e)
                }
            }
        }
    }
}
