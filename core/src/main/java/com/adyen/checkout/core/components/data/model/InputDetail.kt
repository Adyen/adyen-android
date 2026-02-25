/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/12/2020.
 */
package com.adyen.checkout.core.components.data.model

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.common.internal.model.ModelUtils.serializeOptList
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

// TODO - Remove this model when PaymentMethod is removed.
//  No need to have this, since it has been removed from the API level 68.
@Parcelize
data class InputDetail(
    val items: List<Item>? = null,
) : ModelObject() {

    companion object {
        private const val ITEMS = "items"

        @JvmField
        val SERIALIZER: Serializer<InputDetail> = object : Serializer<InputDetail> {
            override fun serialize(modelObject: InputDetail): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(ITEMS, serializeOptList(modelObject.items, Item.SERIALIZER))
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(InputDetail::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): InputDetail {
                return InputDetail(
                    items = deserializeOptList(jsonObject.optJSONArray(ITEMS), Item.SERIALIZER),
                )
            }
        }
    }
}
