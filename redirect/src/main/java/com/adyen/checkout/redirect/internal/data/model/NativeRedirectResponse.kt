/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/10/2023.
 */

package com.adyen.checkout.redirect.internal.data.model

import com.adyen.checkout.core.common.internal.model.ModelObject
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
internal data class NativeRedirectResponse(
    val redirectResult: String,
) : ModelObject() {

    companion object {
        private const val REDIRECT_RESULT = "redirectResult"

        @JvmField
        val SERIALIZER: Serializer<NativeRedirectResponse> =
            object : Serializer<NativeRedirectResponse> {
                override fun serialize(modelObject: NativeRedirectResponse): JSONObject {
                    return JSONObject().apply {
                        putOpt(REDIRECT_RESULT, modelObject.redirectResult)
                    }
                }

                override fun deserialize(jsonObject: JSONObject): NativeRedirectResponse {
                    return NativeRedirectResponse(
                        redirectResult = jsonObject.getString(REDIRECT_RESULT),
                    )
                }
            }
    }
}
