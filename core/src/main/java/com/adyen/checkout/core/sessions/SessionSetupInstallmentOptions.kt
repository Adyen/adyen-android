/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 24/2/2023.
 */

package com.adyen.checkout.core.sessions

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.JsonUtils
import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getIntOrNull
import com.adyen.checkout.core.common.internal.model.optIntList
import com.adyen.checkout.core.common.internal.model.optStringList
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupResponse
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class SessionSetupInstallmentOptions(
    val plans: List<String>?,
    val preselectedValue: Int?,
    val values: List<Int>?
) : ModelObject() {

    companion object {
        private const val PLANS = "plans"
        private const val PRESELECTED_VALUE = "preselectedValue"
        private const val VALUES = "values"

        @JvmField
        val SERIALIZER: Serializer<SessionSetupInstallmentOptions> =
            object : Serializer<SessionSetupInstallmentOptions> {
                override fun serialize(modelObject: SessionSetupInstallmentOptions): JSONObject {
                    return try {
                        JSONObject().apply {
                            putOpt(PLANS, JsonUtils.serializeOptStringList(modelObject.plans))
                            putOpt(PRESELECTED_VALUE, modelObject.preselectedValue)
                            putOpt(VALUES, JsonUtils.serializeOptIntegerList(modelObject.values))
                        }
                    } catch (e: JSONException) {
                        throw ModelSerializationException(SessionSetupResponse::class.java, e)
                    }
                }

                override fun deserialize(jsonObject: JSONObject): SessionSetupInstallmentOptions {
                    return try {
                        SessionSetupInstallmentOptions(
                            plans = jsonObject.optStringList(PLANS).orEmpty(),
                            preselectedValue = jsonObject.getIntOrNull(PRESELECTED_VALUE),
                            values = jsonObject.optIntList(VALUES),
                        )
                    } catch (e: JSONException) {
                        throw ModelSerializationException(SessionSetupConfiguration::class.java, e)
                    }
                }
            }
    }
}
