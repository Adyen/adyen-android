/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/10/2026.
 */

package com.adyen.checkout.address.internal.data.model

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * An entry of the datasets API. For countries [id] is the ISO 3166-1 alpha-2 code, for states it is the ISO 3166-2
 * subdivision code.
 */
@Parcelize
internal data class AddressItem(
    val id: String?,
    val name: String?,
) : ModelObject() {

    companion object {
        private const val ID = "id"
        private const val NAME = "name"

        @JvmField
        val SERIALIZER: Serializer<AddressItem> = object : Serializer<AddressItem> {
            override fun serialize(modelObject: AddressItem): JSONObject {
                return JSONObject().apply {
                    putOpt(ID, modelObject.id)
                    putOpt(NAME, modelObject.name)
                }
            }

            override fun deserialize(jsonObject: JSONObject): AddressItem {
                return AddressItem(
                    id = jsonObject.getStringOrNull(ID),
                    name = jsonObject.getStringOrNull(NAME),
                )
            }
        }
    }
}
