/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/10/2026.
 */

package com.adyen.checkout.dropin.internal.data.model

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
internal data class DisableTokenResponse(
    val sessionData: String?,
) : ModelObject() {

    companion object {
        private const val SESSION_DATA = "sessionData"

        @JvmField
        val SERIALIZER: Serializer<DisableTokenResponse> = object : Serializer<DisableTokenResponse> {
            override fun serialize(modelObject: DisableTokenResponse): JSONObject {
                return JSONObject().apply {
                    putOpt(SESSION_DATA, modelObject.sessionData)
                }
            }

            override fun deserialize(jsonObject: JSONObject): DisableTokenResponse {
                return DisableTokenResponse(
                    sessionData = jsonObject.getStringOrNull(SESSION_DATA),
                )
            }
        }
    }
}
