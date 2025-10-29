/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/10/2023.
 */

package com.adyen.checkout.redirect.internal.data.model

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.ModelObject
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class NativeRedirectRequest(
    val redirectData: String?,
    val returnQueryString: String,
) : ModelObject() {

    companion object {
        private const val REDIRECT_DATA = "redirectData"
        private const val RETURN_QUERY_STRING = "returnQueryString"

        @JvmField
        val SERIALIZER: Serializer<NativeRedirectRequest> =
            object : Serializer<NativeRedirectRequest> {
                override fun serialize(modelObject: NativeRedirectRequest): JSONObject {
                    val jsonObject = JSONObject()
                    try {
                        jsonObject.putOpt(REDIRECT_DATA, modelObject.redirectData)
                        jsonObject.putOpt(RETURN_QUERY_STRING, modelObject.returnQueryString)
                    } catch (e: JSONException) {
                        throw ModelSerializationException(NativeRedirectRequest::class.java, e)
                    }
                    return jsonObject
                }

                override fun deserialize(jsonObject: JSONObject): NativeRedirectRequest {
                    return try {
                        NativeRedirectRequest(
                            redirectData = jsonObject.getString(REDIRECT_DATA),
                            returnQueryString = jsonObject.getString(RETURN_QUERY_STRING),
                        )
                    } catch (e: JSONException) {
                        throw ModelSerializationException(NativeRedirectRequest::class.java, e)
                    }
                }
            }
    }
}
