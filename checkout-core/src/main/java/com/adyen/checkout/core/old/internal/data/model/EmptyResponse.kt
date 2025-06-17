/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 13/2/2024.
 */

package com.adyen.checkout.core.old.internal.data.model

import androidx.annotation.RestrictTo
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class EmptyResponse : ModelObject() {
    companion object {
        @JvmField
        val SERIALIZER: Serializer<EmptyResponse> = object : Serializer<EmptyResponse> {
            override fun serialize(modelObject: EmptyResponse) = JSONObject()

            override fun deserialize(jsonObject: JSONObject) = EmptyResponse()
        }
    }
}
