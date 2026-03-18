/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/4/2022.
 */

package com.adyen.checkout.core.common.internal.data.model

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
internal data class PublicKeyResponse(
    val publicKey: String
) : ModelObject() {

    companion object {

        private const val PUBLIC_KEY = "publicKey"

        @JvmField
        val SERIALIZER: Serializer<PublicKeyResponse> = object : Serializer<PublicKeyResponse> {
            override fun serialize(modelObject: PublicKeyResponse): JSONObject {
                return JSONObject().apply {
                    putOpt(PUBLIC_KEY, modelObject.publicKey)
                }
            }

            override fun deserialize(jsonObject: JSONObject): PublicKeyResponse {
                return PublicKeyResponse(
                    publicKey = jsonObject.getStringOrNull(PUBLIC_KEY).orEmpty(),
                )
            }
        }
    }
}
