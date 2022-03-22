/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/12/2020.
 */
package com.adyen.checkout.components.model.paymentmethods

import android.os.Parcel
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.model.ModelUtils.serializeOptList
import org.json.JSONException
import org.json.JSONObject

data class InputDetail(
    var items: List<Item>? = null,
) : ModelObject() {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val ITEMS = "items"

        @JvmField
        val CREATOR = Creator(InputDetail::class.java)

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
