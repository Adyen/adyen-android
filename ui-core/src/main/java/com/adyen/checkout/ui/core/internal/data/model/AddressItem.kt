/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 13/1/2023.
 */

package com.adyen.checkout.ui.core.internal.data.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class AddressItem(
    val id: String? = null,
    val name: String? = null
) : ModelObject() {

    companion object {
        private const val ID = "id"
        private const val NAME = "name"

        @JvmField
        val SERIALIZER: Serializer<AddressItem> = object : Serializer<AddressItem> {
            override fun serialize(modelObject: AddressItem): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(ID, modelObject.id)
                    jsonObject.putOpt(NAME, modelObject.name)
                } catch (e: JSONException) {
                    throw ModelSerializationException(AddressItem::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): AddressItem {
                return try {
                    AddressItem(
                        id = jsonObject.getStringOrNull(ID),
                        name = jsonObject.getStringOrNull(NAME)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(AddressItem::class.java, e)
                }
            }
        }
    }
}
