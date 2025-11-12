/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/10/2023.
 */

package com.adyen.checkout.redirect.old.internal.data.model

import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import kotlinx.parcelize.Parcelize
import org.json.JSONException
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
                    val jsonObject = JSONObject()
                    try {
                        jsonObject.putOpt(REDIRECT_RESULT, modelObject.redirectResult)
                    } catch (e: JSONException) {
                        throw ModelSerializationException(NativeRedirectResponse::class.java, e)
                    }
                    return jsonObject
                }

                override fun deserialize(jsonObject: JSONObject): NativeRedirectResponse {
                    return try {
                        NativeRedirectResponse(
                            redirectResult = jsonObject.getString(REDIRECT_RESULT),
                        )
                    } catch (e: JSONException) {
                        throw ModelSerializationException(NativeRedirectRequest::class.java, e)
                    }
                }
            }
    }
}
